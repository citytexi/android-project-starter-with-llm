#!/usr/bin/env python3
"""Generate an Android project from template/ + variants/ using a PRD file."""
from __future__ import annotations

import argparse
import os
import re
import shutil
import stat
import subprocess
import sys
import tempfile
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from starter_prd import (  # noqa: E402,F401
    Prd, class_prefix, effective_prefix, parse_prd, validate,
)

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
_MIN_SDK_LINE_RE = re.compile(r"(?m)^starter\.minSdk=.*$")
_BASE_URL_LINE_RE = re.compile(r"(?m)^starter\.baseUrl=.*$")
STRINGS_XML = "app/src/main/res/values/strings.xml"
GRADLE_PROPERTIES = "gradle.properties"


REPO_ROOT = Path(__file__).resolve().parent.parent
# Written at the root of every generated project. --overwrite deletes only directories
# that carry it, so it never removes a directory generate.py did not create.
MARKER_NAME = ".starter-generated"
MARKER_TEXT = (
    "Created by scripts/generate.py of android-project-starter-with-llm.\n"
    "generate.py --overwrite deletes a non-empty directory only if this file is present.\n"
)


class TemplateChangedError(ValueError):
    """The template no longer has the shape generate.py edits (a template bug)."""

    def __init__(self, detail: str):
        super().__init__(f"template changed: {detail}")


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


def _read_text(path: Path) -> str:
    # newline="" keeps CRLF as is, so files are rewritten with their own line endings.
    with open(path, encoding="utf-8", newline="") as f:
        return f.read()


def _write_text(path: Path, text: str) -> None:
    with open(path, "w", encoding="utf-8", newline="") as f:
        f.write(text)


def _decode_text(path: Path) -> str | None:
    """File content if it is UTF-8 text, None for binary files (NUL byte or not UTF-8)."""
    data = path.read_bytes()
    if b"\0" in data:
        return None
    try:
        return data.decode("utf-8")
    except UnicodeDecodeError:
        return None


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
        # Scan every UTF-8 file, not only the suffixes that _replace_text rewrites,
        # so a token in e.g. an extension-less script is reported instead of shipped.
        text = _decode_text(p)
        if text is None:
            continue
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


def _check_output_dir(out: Path, repo_root: Path, overwrite: bool) -> str:
    """Validate the output directory and return how to write into it.

    "new": missing or empty. "replace": created by generate.py and --overwrite given
    (deleted first). "merge": the root of a clean git repository (for example a fresh
    clone with README/LICENSE): files are added, same-named tracked files are replaced.
    """
    out_a = Path(os.path.abspath(out))
    repo_a = Path(os.path.abspath(repo_root))
    # The output must not be the repo, one of its ancestors, the FS root or home.
    if out_a.exists():
        danger = [repo_a, *repo_a.parents, Path(out_a.anchor), Path.home()]
        if any(_same(out_a, d) for d in danger):
            raise ValueError(f"refusing to use dangerous output directory: {out_a}")
    elif out_a == Path(out_a.anchor):
        raise ValueError(f"refusing to use dangerous output directory: {out_a}")
    # Inside the repo only <repo>/build/<something> is allowed. Check the resolved path
    # too: a symlink outside the repo can point into one of its subdirectories.
    for candidate in (out_a, out_a.resolve()):
        _check_not_in_repo(candidate, repo_a, out_a)
    if not out_a.exists() or (out_a.is_dir() and not any(out_a.iterdir())):
        return "new"
    if out_a.is_dir() and (out_a / MARKER_NAME).is_file():
        if not overwrite:
            raise FileExistsError(
                f"output directory is not empty: {out_a} "
                "(it was created by generate.py: use --overwrite to replace it)"
            )
        return "replace"
    clean = _git_root_clean(out_a) if out_a.is_dir() else None
    if clean is True:
        if _git_branch_exists(out_a, WORK_BRANCH):
            raise ValueError(
                f"branch '{WORK_BRANCH}' already exists in {out_a}: delete or rename it, "
                "or merge it first"
            )
        return "merge"
    if clean is False:
        raise ValueError(
            f"output directory is a git repository with uncommitted or untracked changes: "
            f"{out_a} (commit or stash them first)"
        )
    if overwrite:
        raise ValueError(f"refusing to delete a directory not created by generate.py: {out_a}")
    raise FileExistsError(
        f"output directory is not empty and is not the root of a git repository: {out_a} "
        "(use an empty directory or a clean git clone)"
    )


def _git_out(cwd: Path, *args: str) -> str:
    return subprocess.run(
        ["git", *args], cwd=cwd, check=True,
        stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, errors="replace",
    ).stdout


def _git_branch_exists(out: Path, branch: str) -> bool:
    try:
        subprocess.run(
            ["git", "rev-parse", "--verify", "--quiet", f"refs/heads/{branch}"], cwd=out,
            check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL,
        )
    except subprocess.CalledProcessError:
        return False
    return True


def _git_root_clean(out: Path) -> bool | None:
    """None if out is not the root of a git work tree, else whether it is clean."""
    if not (out / ".git").exists():
        return None
    try:
        top = _git_out(out, "rev-parse", "--show-toplevel").strip()
        status = _git_out(out, "status", "--porcelain", "--untracked-files=all")
    except (subprocess.CalledProcessError, OSError) as e:
        detail = (getattr(e, "stderr", None) or str(e)).strip()
        raise ValueError(f"cannot read the git repository in {out}: {detail}") from e
    if not _same(Path(top), out):
        return None
    return status.strip() == ""


def _merge_conflicts(staging: Path, out: Path) -> tuple[list[str], list[str]]:
    """(replaced, blocking) relative paths for copying staging into an existing repo.

    replaced: files git tracks that the template replaces (old versions stay in history).
    blocking: paths that cannot be replaced safely (ignored files, directories,
    symlinks, a file where the template needs a directory).
    """
    tracked = set(_git_out(out, "ls-files", "-z").split("\0"))
    replaced, blocking = [], []
    for p in _iter_files(staging):
        rel = p.relative_to(staging)
        cur = out
        for i, part in enumerate(rel.parts):
            cur = cur / part
            last = i == len(rel.parts) - 1
            if cur.is_symlink():
                blocking.append(f"{Path(*rel.parts[: i + 1]).as_posix()} (symlink)")
                break
            if not cur.exists():
                break
            if not last and not cur.is_dir():
                blocking.append(f"{Path(*rel.parts[: i + 1]).as_posix()} (file, directory needed)")
                break
            if last:
                if cur.is_dir():
                    blocking.append(f"{rel.as_posix()} (directory, file needed)")
                elif rel.as_posix() in tracked:
                    replaced.append(rel.as_posix())
                else:
                    blocking.append(f"{rel.as_posix()} (not tracked by git)")
    return replaced, sorted(set(blocking))


def _check_not_in_repo(path: Path, repo_a: Path, shown: Path) -> None:
    for anc, below in _existing_ancestors(path):
        if not _same(anc, repo_a):
            continue
        build = repo_a / "build"
        first = anc / below[0] if below else None
        in_build = first is not None and (
            _same(first, build) if first.exists() else below[0] == "build"
        )
        if not in_build or len(below) < 2:
            raise ValueError(
                f"refusing to write inside the starter repo (only build/<dir> is allowed): {shown}"
            )
        return


def _force_writable(func, path, _exc) -> None:
    """rmtree error handler: make path (and its parent) writable, then retry once.

    Windows refuses to delete read-only files (git marks some objects read-only);
    POSIX refuses to delete entries of a read-only directory.
    """
    parent = os.path.dirname(path)
    if parent:
        os.chmod(parent, os.stat(parent).st_mode | stat.S_IRWXU)
    if not os.path.islink(path):
        os.chmod(path, os.stat(path).st_mode | stat.S_IWRITE)
    func(path)


def _rmtree(path: Path) -> None:
    if sys.version_info >= (3, 12):
        shutil.rmtree(path, onexc=_force_writable)
    else:
        shutil.rmtree(path, onerror=lambda f, p, e: _force_writable(f, p, e[1]))


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
        text = _read_text(p)
        new = text.replace(OLD_PACKAGE, prd.package_name)
        new = new.replace("com/example/starter", prd.package_name.replace(".", "/"))
        new = _PREFIX_RE.sub(prefix, new)
        if new != text:
            _write_text(p, new)


def _sub_once(pattern: re.Pattern, repl, text: str, what: str) -> str:
    new, count = pattern.subn(repl, text)
    if count != 1:
        raise TemplateChangedError(f"expected exactly one {what}, found {count}")
    return new


def _apply_prd_values(root: Path, prd: Prd) -> None:
    """Write user values. Runs after the leftover scan: they may contain `Starter`."""
    strings, props = root / STRINGS_XML, root / GRADLE_PROPERTIES
    for f in (strings, props):
        if not f.is_file():
            raise TemplateChangedError(f"{f.relative_to(root).as_posix()} is missing")
    esc = escape_android_string(prd.app_name)
    text = _read_text(strings)
    _write_text(strings, _sub_once(
        _APP_NAME_RE, lambda m: m.group(1) + esc + m.group(3), text,
        f'<string name="app_name"> in {STRINGS_XML}',
    ))
    text = _read_text(props)
    text = _sub_once(
        _MIN_SDK_LINE_RE, lambda m: f"starter.minSdk={prd.min_sdk}", text,
        f"starter.minSdk= line in {GRADLE_PROPERTIES}",
    )
    text = _sub_once(
        _BASE_URL_LINE_RE, lambda m: f"starter.baseUrl={prd.base_url}", text,
        f"starter.baseUrl= line in {GRADLE_PROPERTIES}",
    )
    _write_text(props, text)


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


def _header_end(lines: list[str]) -> int:
    """Index of the first line after the file header (comments, @file:, package)."""
    i, n = 0, len(lines)
    while i < n:
        stripped = lines[i].strip()
        if not stripped or stripped.startswith("//"):
            i += 1
        elif stripped.startswith("/*"):
            while i < n and "*/" not in lines[i]:
                i += 1
            i += 1
        elif stripped.startswith("@file:"):
            depth = 0
            while i < n:
                depth += lines[i].count("(") - lines[i].count(")")
                i += 1
                if depth <= 0:
                    break
        elif stripped.startswith("package ") or stripped == "package":
            i += 1
        else:
            return i
    return i


def _import_runs(lines: list[str], start: int) -> list[tuple[int, int]]:
    """(first, last) line indexes of each import run in the import region.

    The region starts at `start` and ends before the first line that is not an import,
    a blank line or a comment. Comment lines split runs; blank lines do not.
    """
    runs: list[tuple[int, int]] = []
    first = last = None
    i = start
    while i < len(lines):
        stripped = lines[i].strip()
        if _IMPORT_LINE_RE.match(stripped):
            if first is None:
                first = i
            last = i
        elif not stripped:
            pass
        elif stripped.startswith("//") or stripped.startswith("/*"):
            if first is not None:
                runs.append((first, last))
                first = last = None
            if stripped.startswith("/*"):
                while i < len(lines) and "*/" not in lines[i]:
                    i += 1
        else:
            break
        i += 1
    if first is not None:
        runs.append((first, last))
    return runs


def _sorted_run(lines: list[str]) -> str:
    nl = "\r\n" if lines[0].endswith("\r\n") else "\n"
    seen: set[str] = set()
    unique = []
    for ln in lines:
        if not ln.strip():
            continue
        norm = " ".join(ln.split())
        if norm not in seen:
            seen.add(norm)
            unique.append(ln.strip())
    unique.sort(key=_import_key)
    tail = lines[-1][len(lines[-1].rstrip("\r\n")) :]
    return nl.join(unique) + tail


def sort_kotlin_imports(text: str) -> str:
    """Sort each import run of the file header in ktlint order and drop duplicates.

    Only the header region is touched: imports after the first declaration (for example
    inside a raw string) and comments before `package` stay as they are. Blank lines
    inside a run are removed; a comment line between imports ends the run.
    """
    lines = text.splitlines(keepends=True)
    runs = _import_runs(lines, _header_end(lines))
    if not runs:
        return text
    out: list[str] = []
    pos = 0
    for first, last in runs:
        out.extend(lines[pos:first])
        out.append(_sorted_run(lines[first : last + 1]))
        pos = last + 1
    out.extend(lines[pos:])
    return "".join(out)


def _sort_imports_in_tree(root: Path) -> None:
    for p in _iter_files(root):
        if p.suffix.lower() not in (".kt", ".kts"):
            continue
        text = _read_text(p)
        new = sort_kotlin_imports(text)
        if new != text:
            _write_text(p, new)


_COMMIT_MESSAGES = {
    "init": "chore: initial project from android-project-starter",
    "merge": "chore: add Android project from android-project-starter",
}
GIT_INIT_HINT = (
    'git init -b main && git add -A && git update-index --chmod=+x gradlew && '
    f'git commit -m "{_COMMIT_MESSAGES["init"]}"'
)
# Generating into an existing repository commits on a new branch, never on the
# checked-out one, so the user can review and merge it (for example through a PR).
WORK_BRANCH = "chore/android-project-setup"
GIT_MERGE_HINT = (
    f'git checkout -b {WORK_BRANCH} && git add -A && git update-index --chmod=+x gradlew && '
    f'git commit -m "{_COMMIT_MESSAGES["merge"]}"'
)


def _git_commands(existing_repo: bool) -> list[list[str]]:
    msg = _COMMIT_MESSAGES["merge" if existing_repo else "init"]
    return [
        ["checkout", "-b", WORK_BRANCH] if existing_repo else ["init", "-b", "main"],
        ["add", "-A"],
        # Windows checkouts have no exec bit: record it so gradlew runs on macOS/Linux/CI.
        ["update-index", "--chmod=+x", "gradlew"],
        # The user's signing setup or global hooks must not block or alter the commit.
        ["-c", "commit.gpgsign=false", "commit", "--no-verify", "-m", msg],
    ]


GIT_COMMANDS = tuple(_git_commands(False))


def _git_init(out: Path, existing_repo: bool = False) -> None:
    for args in _git_commands(existing_repo):
        if args[0] == "update-index" and not (out / "gradlew").is_file():
            continue
        # Output is captured and shown only when a command fails (see generate()).
        subprocess.run(
            ["git", *args], cwd=out, check=True,
            stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True, errors="replace",
        )


_IMPORT_PATH_RE = re.compile(r"(?m)^[ \t]*import[ \t]+([\w.`]+)(?:[ \t]+as[ \t]+([\w`]+))?")
_TOKEN_RE = re.compile(r"\bStarter(\w*)")


def _source_roots(prd: Prd, repo_root: Path) -> list[Path]:
    """Directories copied into the output, in copy order (later ones win)."""
    roots = [repo_root / "template", repo_root / "variants" / "navigation" / prd.navigation]
    if prd.ci:
        ci_root = repo_root / "variants" / "ci"
        roots.append(ci_root / "_shared")
        roots.extend(ci_root / name for name in prd.ci)
    return roots


def find_prefix_clashes(roots: list[Path], prefix: str) -> list[str]:
    """Imports that would clash with a renamed `Starter*` name, read from the sources.

    `StarterDispatcher` becomes `<prefix>Dispatcher`. If a Kotlin file uses that name and
    also imports the same simple name from another package (for prefix `Coroutine`:
    `kotlinx.coroutines.CoroutineDispatcher`), the generated file does not compile.
    Returns "file: import <path> clashes with <name> (from Starter<x>)" entries.
    """
    clashes = []
    base = roots[0].parent if roots else Path()
    for root in roots:
        if not root.is_dir():
            continue
        for p in _iter_files(root, skip=True):
            if p.suffix.lower() not in (".kt", ".kts"):
                continue
            text = _read_text(p)
            used = {prefix + m.group(1): m.group(0) for m in _TOKEN_RE.finditer(text)}
            if not used:
                continue
            for m in _IMPORT_PATH_RE.finditer(text):
                path = m.group(1).replace("`", "")
                if path == OLD_PACKAGE or path.startswith(OLD_PACKAGE + "."):
                    continue
                local = (m.group(2) or path.rsplit(".", 1)[-1]).replace("`", "")
                if local in used:
                    rel = p.relative_to(base).as_posix()
                    clashes.append(
                        f"{rel}: import {path} clashes with {local} (from {used[local]})"
                    )
    return clashes


def _build(staging: Path, prd: Prd, roots: list[Path], prefix: str) -> None:
    for root in roots:
        _copy_tree(root, staging)
    _rename_package_dirs(staging, prd.package_name, prefix)
    _replace_text(staging, prd, prefix)
    _sort_imports_in_tree(staging)
    leftovers = find_leftovers(staging)
    if leftovers:
        raise LeftoverTokensError(leftovers)
    _apply_prd_values(staging, prd)


def generate(prd: Prd, repo_root: Path, overwrite: bool, git: bool = True) -> Path:
    out = prd.output_dir
    mode = _check_output_dir(out, repo_root, overwrite)
    prefix = effective_prefix(prd)
    roots = _source_roots(prd, repo_root)
    # Checked on the sources, before anything is written or deleted.
    clashes = find_prefix_clashes(roots, prefix)
    if clashes:
        raise ValueError(
            f"class prefix '{prefix}' clashes with imported names; set classPrefix in the "
            "PRD to another PascalCase name (e.g. the app name):\n  " + "\n  ".join(clashes)
        )
    # Build in a temporary directory: the output is not touched until the project is
    # complete (exit 3 and template errors leave it as it was).
    with tempfile.TemporaryDirectory(prefix="starter-generate-") as tmp:
        staging = Path(tmp) / "project"
        staging.mkdir()
        _build(staging, prd, roots, prefix)
        if mode == "merge":
            replaced, blocking = _merge_conflicts(staging, out)
            if blocking:
                raise ValueError(
                    f"cannot add the project to the git repository {Path(os.path.abspath(out))}: "
                    "these paths are in the way (move or delete them first):\n  "
                    + "\n  ".join(blocking)
                )
            shutil.copytree(staging, out, symlinks=True, dirs_exist_ok=True)
            if replaced:
                print(
                    "note: replaced existing files (the old versions stay in git history): "
                    + ", ".join(replaced),
                    file=sys.stderr,
                )
        else:
            if mode == "replace":
                _rmtree(out)
            out.mkdir(parents=True, exist_ok=True)
            # Written first so a half-copied output (I/O error) can still be overwritten.
            (out / MARKER_NAME).write_text(MARKER_TEXT, encoding="utf-8")
            shutil.copytree(staging, out, symlinks=True, dirs_exist_ok=True)
    if git:
        existing = mode == "merge"
        try:
            _git_init(out, existing_repo=existing)
        except (subprocess.CalledProcessError, OSError) as e:
            output = getattr(e, "output", None)
            if output:
                print(output.rstrip(), file=sys.stderr)
            hint = GIT_MERGE_HINT if existing else GIT_INIT_HINT
            print(
                f"warning: git init/commit failed ({e}); the project was generated. "
                f"Configure git (user.name/user.email) and run: {hint}",
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
        # utf-8-sig drops the BOM that some Windows editors write.
        prd = parse_prd(prd_path.read_text(encoding="utf-8-sig"), prd_path.parent)
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
