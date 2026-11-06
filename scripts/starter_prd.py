"""PRD parsing and validation for the Android project starter generator."""
from __future__ import annotations

import os
import re
from dataclasses import dataclass
from pathlib import Path
from typing import Literal
from urllib.parse import urlsplit

NAVIGATIONS = ("nav2", "nav3")
CI_OPTIONS = ("build-debug", "build-release", "test", "ui-diff")
DEFAULT_MIN_SDK = 26
# Hilt's lifecycle-viewmodel-compose needs API 23; targetSdk is pinned to 36.
MIN_SDK_RANGE = (23, 36)
# The generator swaps the `Starter` class prefix, so a new prefix that starts with it
# would look like a leftover template token.
TEMPLATE_PREFIX = "Starter"
PLATFORM_ROOTS = frozenset({"java", "javax", "kotlin", "android", "androidx"})
_BASE_URL_FORBIDDEN = re.compile(r"""[\s"'\\@?#]""")
KEYS = (
    "appName", "packageName", "classPrefix", "minSdk", "baseUrl", "navigation", "ci",
    "outputDir",
)
_CLASS_PREFIX_RE = re.compile(r"^[A-Z][A-Za-z0-9]*$")
_MIN_SDK_RE = re.compile(r"^[0-9]+$")

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
    # Raw `classPrefix` value; empty means "derive from packageName".
    class_prefix: str = ""


def class_prefix(package_name: str) -> str:
    """Class-name prefix derived from the last package segment (club_moim -> ClubMoim)."""
    last = package_name.split(".")[-1]
    return "".join(p[:1].upper() + p[1:] for p in last.split("_") if p)


def effective_prefix(prd: Prd) -> str:
    """The class prefix the generator uses: `classPrefix` if set, else derived."""
    return prd.class_prefix or class_prefix(prd.package_name)


def _read_properties(text: str) -> dict[str, str]:
    m = _BLOCK_RE.search(text)
    if not m:
        raise ValueError("PRD has no ```properties block")
    first_line = text.count("\n", 0, m.start(1)) + 1
    props: dict[str, str] = {}
    seen: dict[str, int] = {}
    for i, raw in enumerate(m.group(1).splitlines()):
        n = first_line + i
        line = raw.strip()
        if not line or line.startswith("#"):
            continue
        if "=" not in line:
            raise ValueError(f"PRD line {n}: expected 'key = value' or a '#' comment: {line!r}")
        key, value = (part.strip() for part in line.split("=", 1))
        if key not in KEYS:
            raise ValueError(
                f"PRD line {n}: unknown key '{key}' (allowed: {', '.join(KEYS)})"
            )
        if key in seen:
            raise ValueError(f"PRD line {n}: duplicate key '{key}' (first set on line {seen[key]})")
        seen[key] = n
        props[key] = value
    return props


def parse_prd(text: str, base_dir: Path) -> Prd:
    props = _read_properties(text)
    package_name = props.get("packageName", "")
    raw_min = props.get("minSdk", "") or str(DEFAULT_MIN_SDK)
    if not _MIN_SDK_RE.match(raw_min):
        raise ValueError(f"minSdk must be an integer: {raw_min!r}")
    min_sdk = int(raw_min)
    ci = tuple(sorted({c.strip() for c in props.get("ci", "").split(",") if c.strip()}))
    out = props.get("outputDir", "")
    if not out:
        last = package_name.split(".")[-1] if package_name else "app"
        out = f"../{last}"
    output_dir = Path(os.path.normpath(base_dir / Path(out).expanduser()))
    return Prd(
        app_name=props.get("appName", ""),
        package_name=package_name,
        min_sdk=min_sdk,
        base_url=props.get("baseUrl", ""),
        navigation=props.get("navigation", "") or "nav3",  # type: ignore[arg-type]
        ci=ci,
        output_dir=output_dir,
        class_prefix=props.get("classPrefix", ""),
    )


def _package_errors(name: str, check_prefix: bool = True) -> list[str]:
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
    if errors:
        return errors
    if segments[:2] == ["com", "example"]:
        errors.append("packageName must not start with com.example")
    if segments[0] in PLATFORM_ROOTS:
        errors.append(
            f"packageName must not start with '{segments[0]}' "
            f"(reserved: {', '.join(sorted(PLATFORM_ROOTS))})"
        )
    prefix = class_prefix(name)
    if check_prefix and prefix.startswith(TEMPLATE_PREFIX):
        errors.append(
            f"packageName last segment '{segments[-1]}' gives class prefix '{prefix}', "
            f"which must not start with '{TEMPLATE_PREFIX}' (template token): "
            "change packageName or set classPrefix"
        )
    return errors


def _class_prefix_errors(prefix: str) -> list[str]:
    if not _CLASS_PREFIX_RE.match(prefix):
        return [
            f"classPrefix '{prefix}' is invalid: use PascalCase ASCII letters and digits, "
            "starting with an uppercase letter (e.g. Moim)"
        ]
    if prefix.startswith(TEMPLATE_PREFIX):
        return [f"classPrefix '{prefix}' must not start with '{TEMPLATE_PREFIX}' (template token)"]
    return []


def _base_url_ok(url: str) -> bool:
    if not url or not url.isascii() or _BASE_URL_FORBIDDEN.search(url):
        return False
    try:
        parts = urlsplit(url)
        parts.port  # raises ValueError for a non-numeric or out-of-range port
    except ValueError:
        return False
    return parts.scheme == "https" and bool(parts.hostname) and url.endswith("/")


def validate(prd: Prd) -> list[str]:
    errors: list[str] = []
    if not prd.app_name.strip():
        errors.append("appName is required")
    errors.extend(_package_errors(prd.package_name, check_prefix=not prd.class_prefix))
    if prd.class_prefix:
        errors.extend(_class_prefix_errors(prd.class_prefix))
    lo, hi = MIN_SDK_RANGE
    if not lo <= prd.min_sdk <= hi:
        errors.append(f"minSdk must be between {lo} and {hi} (got {prd.min_sdk})")
    if prd.navigation not in NAVIGATIONS:
        errors.append(
            f"navigation '{prd.navigation}' is unknown (expected one of: {', '.join(NAVIGATIONS)})"
        )
    for c in prd.ci:
        if c not in CI_OPTIONS:
            errors.append(f"ci '{c}' is unknown (expected: {', '.join(CI_OPTIONS)})")
    if not _base_url_ok(prd.base_url):
        errors.append(
            "baseUrl must be an https:// URL with a host (and a valid port, if any) "
            "that ends with '/' and has only ASCII characters, no spaces, quotes, "
            "backslashes, '@', '?' or '#' (Retrofit requirement)"
        )
    return errors
