#!/usr/bin/env python3
"""Compare a generated project's structure to template/, then run the Gradle gate."""
import argparse
import os
import re
import subprocess
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
TAIL_LINES = 80

_BLOCK_COMMENT = re.compile(r"/\*.*?\*/", re.S)
_LINE_COMMENT = re.compile(r"//[^\n]*")
_INCLUDE = re.compile(r"\binclude\s*\((.*?)\)", re.S)
_STRING = re.compile(r'"([^"]*)"')


def settings_modules(settings_text: str) -> list[str]:
    text = _BLOCK_COMMENT.sub("", settings_text)
    text = _LINE_COMMENT.sub("", text)
    modules = set()
    for call in _INCLUDE.finditer(text):
        modules.update(_STRING.findall(call.group(1)))
    return sorted(modules)


def _read(path: Path):
    try:
        return path.read_bytes()
    except OSError:
        return None


def compare_structure(project: Path, template: Path) -> list[str]:
    msgs = []
    settings = {}
    for label, root in (("project", project), ("template", template)):
        data = _read(root / "settings.gradle.kts")
        if data is None:
            msgs.append(f"{label}: settings.gradle.kts missing or unreadable")
        else:
            settings[label] = set(settings_modules(data.decode("utf-8", "replace")))
    if len(settings) == 2:
        for m in sorted(settings["project"] - settings["template"]):
            msgs.append(f"module not in template: {m}")
        for m in sorted(settings["template"] - settings["project"]):
            msgs.append(f"module missing from project: {m}")
    cat = Path("gradle") / "libs.versions.toml"
    p, t = _read(project / cat), _read(template / cat)
    if p is None:
        msgs.append("project: gradle/libs.versions.toml missing or unreadable")
    elif t is None:
        msgs.append("template: gradle/libs.versions.toml missing or unreadable")
    elif p != t:
        msgs.append("gradle/libs.versions.toml differs from template")
    return msgs


WRAPPER_SCRIPTS = ("gradlew", "gradlew.bat")


def wrapper_problems(project: Path) -> list[str]:
    """Missing Gradle wrapper scripts (both are part of the generated structure)."""
    return [
        f"project: {name} missing (Gradle wrapper script)"
        for name in WRAPPER_SCRIPTS
        if not (project / name).is_file()
    ]


def gradle_command(platform: str, project: Path) -> list[str]:
    name = "gradlew.bat" if platform == "win32" else "gradlew"
    return [str(project / name), "verifyStarter", "--continue"]


def run_gradle(project: Path) -> int:
    cmd = gradle_command(sys.platform, project)
    if sys.platform != "win32" and not os.access(cmd[0], os.X_OK):
        cmd = ["sh", *cmd]
    try:
        proc = subprocess.run(
            cmd, cwd=project, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
            text=True, errors="replace",
        )
    except OSError as e:
        print(f"error: cannot run gradle: {e}", file=sys.stderr)
        return 5
    if proc.returncode != 0:
        lines = (proc.stdout or "").splitlines()[-TAIL_LINES:]
        print("\n".join(lines), file=sys.stderr)
        print(f"error: gradle exited with {proc.returncode}", file=sys.stderr)
        return 5
    return 0


def main(argv=None, repo_root=None) -> int:
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("project", type=Path)
    args = ap.parse_args(argv)
    project = args.project.resolve()
    template = (repo_root or REPO_ROOT) / "template"
    if not project.is_dir():
        print(f"error: project dir not found: {project}", file=sys.stderr)
        return 4
    problems = compare_structure(project, template) + wrapper_problems(project)
    if problems:
        for m in problems:
            print(f"error: {m}", file=sys.stderr)
        return 4
    code = run_gradle(project)
    if code == 0:
        print("verify: ok")
    return code


if __name__ == "__main__":
    sys.exit(main())
