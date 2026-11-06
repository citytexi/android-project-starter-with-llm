#!/usr/bin/env python3
"""Generate an Android project from template/ + variants/ using a PRD file."""
from __future__ import annotations

import argparse
import os
import re
import shutil
import subprocess
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from starter_prd import Prd, class_prefix, parse_prd, validate  # noqa: E402,F401

TEXT_EXTENSIONS = {
    ".kt", ".kts", ".xml", ".toml", ".md", ".pro", ".properties",
    ".yml", ".yaml", ".py", ".json", ".txt",
}
SKIP_NAMES = {"build", ".gradle", ".idea", ".git", "local.properties", "__pycache__"}
OLD_PACKAGE = "com.example.starter"
OLD_PACKAGE_PATH = ("com", "example", "starter")
_PREFIX_RE = re.compile(r"\bStarter")
_LEFTOVER_RE = re.compile(r"com\.example\.starter|\bStarter")
_APP_NAME_RE = re.compile(r'(<string\s+name="app_name"[^>]*>)(.*?)(</string>)', re.DOTALL)


REPO_ROOT = Path(__file__).resolve().parent.parent
# Written at the root of every generated project. --overwrite deletes only directories
# that carry it, so it never removes a directory generate.py did not create.
MARKER_NAME = ".starter-generated"
MARKER_TEXT = (
    "Created by scripts/generate.py of android-project-starter-with-llm.\n"
    "generate.py --overwrite deletes a non-empty directory only if this file is present.\n"
)


class LeftoverTokensError(RuntimeError):
    def __init__(self, leftovers: list[str]):
        super().__init__("leftover template tokens: " + ", ".join(leftovers))
        self.leftovers = leftovers


def escape_android_string(value: str) -> str:
    out = []
    for i, ch in enumerate(value):
        if ch == "&":
            out.append("&amp;")
        elif ch == "<":
            out.append("&lt;")
        elif ch == "\\":
            out.append("\\\\")
        elif ch == "'":
            out.append("\\'")
        elif ch == '"':
            out.append('\\"')
        elif ch == "\n":
            out.append("\\n")
        elif i == 0 and ch in "@?":
            out.append("\\" + ch)
        else:
            out.append(ch)
    return "".join(out)


def _is_text(path: Path) -> bool:
    return path.suffix.lower() in TEXT_EXTENSIONS


def _iter_files(root: Path, skip: bool = False):
    """Yield files under root. skip=True drops build outputs (copy source only)."""
    for p in sorted(root.rglob("*")):
        if not p.is_file():
            continue
        if skip and any(part in SKIP_NAMES for part in p.relative_to(root).parts):
            continue
        yield p


def find_leftovers(root: Path) -> list[str]:
    found = []
    for p in _iter_files(root):
        rel = p.relative_to(root)
        if any(_LEFTOVER_RE.search(part) for part in rel.parts):
            found.append(f"{rel.as_posix()}:0")
        if not _is_text(p):
            continue
        text = p.read_text(encoding="utf-8", errors="replace")
        for n, line in enumerate(text.splitlines(), 1):
            if _LEFTOVER_RE.search(line):
                found.append(f"{p.relative_to(root).as_posix()}:{n}")
    return found


def _copy_tree(src: Path, dst: Path) -> None:
    if not src.is_dir():
        raise FileNotFoundError(f"missing directory: {src}")
    for p in _iter_files(src, skip=True):
        target = dst / p.relative_to(src)
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(p, target)


def _same(a: Path, b: Path) -> bool:
    """True if both paths exist and are the same file system object.

    Compares inode/device, so case variants on case-insensitive file systems
    (macOS APFS, Windows NTFS) and symlinks count as the same directory.
    """
    try:
        return os.path.samefile(a, b)
    except OSError:
        return False


def _existing_ancestors(path: Path):
    """Yield (ancestor, parts below it) for path and each parent that exists."""
    parts: list[str] = []
    cur = path
    while True:
        if cur.exists():
            yield cur, tuple(reversed(parts))
        if cur.parent == cur:
            return
        parts.append(cur.name)
        cur = cur.parent


def _check_output_dir(out: Path, repo_root: Path, overwrite: bool) -> None:
    out_a = Path(os.path.abspath(out))
    repo_a = Path(os.path.abspath(repo_root))
    # The output must not be the repo, one of its ancestors, the FS root or home.
    if out_a.exists():
        danger = [repo_a, *repo_a.parents, Path(out_a.anchor), Path.home()]
        if any(_same(out_a, d) for d in danger):
            raise ValueError(f"refusing to use dangerous output directory: {out_a}")
    elif out_a == Path(out_a.anchor):
        raise ValueError(f"refusing to use dangerous output directory: {out_a}")
    # Inside the repo only <repo>/build/<something> is allowed.
    for anc, below in _existing_ancestors(out_a):
        if not _same(anc, repo_a):
            continue
        build = repo_a / "build"
        first = anc / below[0] if below else None
        in_build = first is not None and (
            _same(first, build) if first.exists() else below[0] == "build"
        )
        if not in_build or len(below) < 2:
            raise ValueError(
                f"refusing to write inside the starter repo (only build/<dir> is allowed): {out_a}"
            )
        break
    if out_a.exists() and (not out_a.is_dir() or any(out_a.iterdir())):
        if not overwrite:
            raise FileExistsError(f"output directory is not empty: {out_a} (use --overwrite)")
        if not out_a.is_dir() or not (out_a / MARKER_NAME).is_file():
            raise ValueError(
                f"refusing to delete a directory not created by generate.py: {out_a}"
            )


def _rename_package_dirs(root: Path, new_package: str, prefix: str) -> None:
    new_parts = new_package.split(".")
    olds = [
        d for d in root.rglob("starter")
        if d.is_dir() and d.relative_to(root).parts[-3:] == OLD_PACKAGE_PATH
    ]
    for old in olds:
        base = old.parent.parent.parent
        staging = base / "__pkg_staging__"
        old.rename(staging)
        for d in (old.parent, old.parent.parent):  # example, com (if now empty)
            try:
                d.rmdir()
            except OSError:
                pass
        dest = base.joinpath(*new_parts)
        dest.parent.mkdir(parents=True, exist_ok=True)
        if dest.exists():
            shutil.copytree(staging, dest, dirs_exist_ok=True)
            shutil.rmtree(staging)
        else:
            staging.rename(dest)
    # rename Starter* file and directory names, deepest first
    for p in sorted(root.rglob("*"), key=lambda x: len(x.parts), reverse=True):
        new_name = _PREFIX_RE.sub(prefix, p.name.replace(OLD_PACKAGE, new_package))
        if new_name != p.name:
            p.rename(p.with_name(new_name))


def _replace_text(root: Path, prd: Prd, prefix: str) -> None:
    for p in _iter_files(root):
        if not _is_text(p):
            continue
        text = p.read_text(encoding="utf-8")
        new = text.replace(OLD_PACKAGE, prd.package_name)
        new = new.replace("com/example/starter", prd.package_name.replace(".", "/"))
        new = _PREFIX_RE.sub(prefix, new)
        rel = p.relative_to(root).as_posix()
        if rel == "app/src/main/res/values/strings.xml":
            esc = escape_android_string(prd.app_name)
            new = _APP_NAME_RE.sub(lambda m: m.group(1) + esc + m.group(3), new)
        elif rel == "gradle.properties":
            new = re.sub(r"(?m)^starter\.minSdk=.*$", lambda m: f"starter.minSdk={prd.min_sdk}", new)
            new = re.sub(r"(?m)^starter\.baseUrl=.*$", lambda m: f"starter.baseUrl={prd.base_url}", new)
        if new != text:
            p.write_text(new, encoding="utf-8", newline="")


_IMPORT_LINE_RE = re.compile(r"^import\s+(\S.*?)\s*$")


def _import_key(line: str) -> tuple[int, str, str]:
    """Sort key matching ktlint's default layout `*,java.**,javax.**,kotlin.**,^`.

    ktlint compares the whole import path as a plain string (so `$` < `.` < `*` < A < a),
    with backticks removed. Verified against ktlintFormat output.
    """
    body = _IMPORT_LINE_RE.match(line.strip()).group(1).split("//")[0].strip()
    path, sep, alias = body.partition(" as ")
    path = path.strip().replace("`", "")
    if sep:
        return (4, path, alias.strip().replace("`", ""))
    for group, prefix in ((1, "java."), (2, "javax."), (3, "kotlin.")):
        if path.startswith(prefix):
            return (group, path, "")
    return (0, path, "")


def sort_kotlin_imports(text: str) -> str:
    """Rewrite the import block as one contiguous, ktlint-ordered, de-duplicated block."""
    lines = text.splitlines(keepends=True)
    first = next((i for i, ln in enumerate(lines) if _IMPORT_LINE_RE.match(ln.strip())), None)
    if first is None:
        return text
    last = first
    for i in range(first, len(lines)):
        stripped = lines[i].strip()
        if _IMPORT_LINE_RE.match(stripped):
            last = i
        elif stripped:
            break
    block = [ln for ln in lines[first : last + 1] if ln.strip()]
    nl = "\r\n" if lines[first].endswith("\r\n") else "\n"
    seen: set[str] = set()
    unique = []
    for ln in block:
        norm = " ".join(ln.split())
        if norm not in seen:
            seen.add(norm)
            unique.append(ln.strip())
    unique.sort(key=_import_key)
    tail = lines[last][len(lines[last].rstrip("\r\n")) :]
    new_block = nl.join(unique) + tail
    return "".join(lines[:first]) + new_block + "".join(lines[last + 1 :])


def _sort_imports_in_tree(root: Path) -> None:
    for p in _iter_files(root):
        if p.suffix.lower() not in (".kt", ".kts"):
            continue
        text = p.read_text(encoding="utf-8")
        new = sort_kotlin_imports(text)
        if new != text:
            p.write_text(new, encoding="utf-8", newline="")


GIT_INIT_HINT = (
    'git init -b main && git add -A && '
    'git commit -m "chore: initial project from android-project-starter"'
)


def _git_init(out: Path) -> None:
    subprocess.run(["git", "init", "-b", "main"], cwd=out, check=True)
    subprocess.run(["git", "add", "-A"], cwd=out, check=True)
    subprocess.run(
        ["git", "commit", "-m", "chore: initial project from android-project-starter"],
        cwd=out, check=True,
    )


def generate(prd: Prd, repo_root: Path, overwrite: bool, git: bool = True) -> Path:
    out = prd.output_dir
    _check_output_dir(out, repo_root, overwrite)
    if out.exists():
        shutil.rmtree(out)
    out.mkdir(parents=True)
    # Written first so a half-finished output (exit 3, I/O error) can still be overwritten.
    (out / MARKER_NAME).write_text(MARKER_TEXT, encoding="utf-8")
    _copy_tree(repo_root / "template", out)
    _copy_tree(repo_root / "variants" / "navigation" / prd.navigation, out)
    if prd.ci:
        ci_root = repo_root / "variants" / "ci"
        _copy_tree(ci_root / "_shared", out)
        for name in prd.ci:
            _copy_tree(ci_root / name, out)
    prefix = class_prefix(prd.package_name)
    _rename_package_dirs(out, prd.package_name, prefix)
    _replace_text(out, prd, prefix)
    _sort_imports_in_tree(out)
    leftovers = find_leftovers(out)
    if leftovers:
        raise LeftoverTokensError(leftovers)
    if git:
        try:
            _git_init(out)
        except (subprocess.CalledProcessError, OSError) as e:
            print(
                f"warning: git init/commit failed ({e}); the project was generated. "
                f"Configure git (user.name/user.email) and run: {GIT_INIT_HINT}",
                file=sys.stderr,
            )
    return out


def main(argv: list[str] | None = None, repo_root: Path | None = None) -> int:
    ap = argparse.ArgumentParser(description="Generate an Android project from a PRD.")
    ap.add_argument("--prd", required=True, type=Path)
    ap.add_argument("--overwrite", action="store_true")
    args = ap.parse_args(argv)
    prd_path = args.prd.resolve()
    try:
        prd = parse_prd(prd_path.read_text(encoding="utf-8"), prd_path.parent)
    except (ValueError, OSError) as e:
        print(f"error: {e}", file=sys.stderr)
        return 2
    errors = validate(prd)
    if errors:
        for e in errors:
            print(f"error: {e}", file=sys.stderr)
        return 2
    repo_root = repo_root or REPO_ROOT
    try:
        out = generate(prd, repo_root, args.overwrite)
    except (FileExistsError, FileNotFoundError, ValueError, OSError) as e:
        print(f"error: {e}", file=sys.stderr)
        return 2
    except LeftoverTokensError as e:
        print("error: template tokens left behind:", file=sys.stderr)
        for l in e.leftovers:
            print(f"  {l}", file=sys.stderr)
        return 3
    print(f"generated: {out}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
