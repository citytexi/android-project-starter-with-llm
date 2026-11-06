"""PRD parsing and validation for the Android project starter generator."""
from __future__ import annotations

import os
import re
from dataclasses import dataclass
from pathlib import Path
from typing import Literal

NAVIGATIONS = ("nav2", "nav3")
CI_OPTIONS = ("build-debug", "build-release", "test", "ui-diff")
DEFAULT_MIN_SDK = 26

_KOTLIN_KEYWORDS = {
    "as", "break", "class", "continue", "do", "else", "false", "for", "fun",
    "if", "in", "interface", "is", "null", "object", "package", "return",
    "super", "this", "throw", "true", "try", "typealias", "typeof", "val",
    "var", "when", "while",
}
_JAVA_KEYWORDS = {
    "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
    "class", "const", "continue", "default", "do", "double", "else", "enum",
    "extends", "final", "finally", "float", "for", "goto", "if", "implements",
    "import", "instanceof", "int", "interface", "long", "native", "new",
    "package", "private", "protected", "public", "return", "short", "static",
    "strictfp", "super", "switch", "synchronized", "this", "throw", "throws",
    "transient", "try", "void", "volatile", "while", "true", "false", "null",
    "_",
}
RESERVED_WORDS = frozenset(_KOTLIN_KEYWORDS | _JAVA_KEYWORDS)
_SEGMENT_RE = re.compile(r"^[a-z][a-z0-9_]*$")
_BLOCK_RE = re.compile(r"```properties[ \t]*\r?\n(.*?)```", re.DOTALL)


@dataclass(frozen=True)
class Prd:
    app_name: str
    package_name: str
    min_sdk: int
    base_url: str
    navigation: Literal["nav2", "nav3"]
    ci: tuple[str, ...]
    output_dir: Path


def _read_properties(text: str) -> dict[str, str]:
    m = _BLOCK_RE.search(text)
    if not m:
        raise ValueError("PRD has no ```properties block")
    props: dict[str, str] = {}
    for raw in m.group(1).splitlines():
        line = raw.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        props[key.strip()] = value.strip()
    return props


def parse_prd(text: str, base_dir: Path) -> Prd:
    props = _read_properties(text)
    package_name = props.get("packageName", "")
    raw_min = props.get("minSdk", "") or str(DEFAULT_MIN_SDK)
    try:
        min_sdk = int(raw_min)
    except ValueError:
        raise ValueError(f"minSdk must be an integer: {raw_min!r}") from None
    ci = tuple(sorted({c.strip() for c in props.get("ci", "").split(",") if c.strip()}))
    out = props.get("outputDir", "")
    if not out:
        last = package_name.split(".")[-1] if package_name else "app"
        out = f"../{last}"
    output_dir = Path(os.path.normpath(base_dir / out))
    return Prd(
        app_name=props.get("appName", ""),
        package_name=package_name,
        min_sdk=min_sdk,
        base_url=props.get("baseUrl", ""),
        navigation=props.get("navigation", "") or "nav3",  # type: ignore[arg-type]
        ci=ci,
        output_dir=output_dir,
    )


def _package_errors(name: str) -> list[str]:
    if not name:
        return ["packageName is required: ask the user to choose one (see AGENTS.md)"]
    segments = name.split(".")
    if len(segments) < 2:
        return [f"packageName '{name}' needs at least 2 segments (e.g. com.club.app)"]
    errors = []
    for seg in segments:
        if not _SEGMENT_RE.match(seg):
            errors.append(
                f"packageName segment '{seg}' is invalid: use lowercase letters, "
                "digits and '_', starting with a letter"
            )
        elif seg in RESERVED_WORDS:
            errors.append(f"packageName segment '{seg}' is a reserved word")
    if not errors and segments[:2] == ["com", "example"]:
        errors.append("packageName must not start with com.example")
    return errors


def validate(prd: Prd) -> list[str]:
    errors: list[str] = []
    if not prd.app_name.strip():
        errors.append("appName is required")
    errors.extend(_package_errors(prd.package_name))
    if prd.navigation not in NAVIGATIONS:
        errors.append(
            f"navigation '{prd.navigation}' is unknown (expected one of: {', '.join(NAVIGATIONS)})"
        )
    for c in prd.ci:
        if c not in CI_OPTIONS:
            errors.append(f"ci '{c}' is unknown (expected: {', '.join(CI_OPTIONS)})")
    if not prd.base_url.startswith("https://") or not prd.base_url.endswith("/"):
        errors.append("baseUrl must start with https:// and end with '/' (Retrofit requirement)")
    return errors
