import contextlib
import io
import os
import re
import shutil
import stat
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path
from unittest import mock

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

import generate  # noqa: E402
import starter_prd  # noqa: E402
from starter_prd import Prd  # noqa: E402

REPO = Path(__file__).resolve().parent.parent.parent
FIXTURE = Path(__file__).resolve().parent / "fixtures" / "mini_template"


def make_prd(**kw):
    base = dict(
        app_name="Dev Sample",
        package_name="io.github.club.devsample",
        min_sdk=26,
        base_url="https://a.com/",
        navigation="nav3",
        ci=(),
        output_dir=Path("out"),
    )
    base.update(kw)
    return Prd(**base)


PRD_TEXT = """# PRD

intro text

```properties
# comment
appName = My App
packageName = io.github.club.devsample
minSdk = 28
baseUrl = https://api.a.com/
navigation = nav2
ci = test, build-debug
outputDir = ../x
```

```properties
appName = Second
```
"""


class ParseTest(unittest.TestCase):
    def test_parse_prd_reads_properties_block(self):
        base = Path("/tmp/work/prd")
        prd = starter_prd.parse_prd(PRD_TEXT, base)
        self.assertEqual(prd.app_name, "My App")
        self.assertEqual(prd.package_name, "io.github.club.devsample")
        self.assertEqual(prd.min_sdk, 28)
        self.assertEqual(prd.base_url, "https://api.a.com/")
        self.assertEqual(prd.navigation, "nav2")
        self.assertEqual(prd.ci, ("build-debug", "test"))
        self.assertEqual(prd.output_dir, base.parent / "x")

    def test_parse_prd_defaults(self):
        text = "```properties\nappName = A\npackageName = io.github.club.devsample\n"
        text += "baseUrl = https://a.com/\nnavigation = nav3\nci =\n```\n"
        base = Path("/tmp/work/prd")
        prd = starter_prd.parse_prd(text, base)
        self.assertEqual(prd.min_sdk, 26)
        self.assertEqual(prd.ci, ())
        self.assertEqual(prd.output_dir, base.parent / "devsample")

    def test_parse_empty_package_name(self):
        prd = starter_prd.parse_prd("```properties\npackageName =\n```", Path("/t/a"))
        self.assertEqual(prd.package_name, "")

    def parse_error(self, body):
        with self.assertRaises(ValueError) as cm:
            starter_prd.parse_prd("# PRD\n\n```properties\n" + body + "```\n", Path("/t/a"))
        return str(cm.exception)

    def test_parse_rejects_unknown_key_with_line_number(self):
        msg = self.parse_error("appName = A\nappname = B\n")
        self.assertIn("PRD line 5: unknown key 'appname'", msg)
        self.assertIn("classPrefix", msg)

    def test_parse_rejects_duplicate_key(self):
        msg = self.parse_error("appName = A\n# c\nappName = B\n")
        self.assertIn("PRD line 6: duplicate key 'appName' (first set on line 4)", msg)

    def test_parse_rejects_line_without_equals(self):
        msg = self.parse_error("appName = A\npackageName io.club.app\n")
        self.assertIn("PRD line 5:", msg)
        self.assertIn("packageName io.club.app", msg)

    def test_parse_rejects_empty_key(self):
        self.assertIn("unknown key ''", self.parse_error("= x\n"))

    def test_parse_crlf(self):
        text = PRD_TEXT.replace("\n", "\r\n")
        prd = starter_prd.parse_prd(text, Path("/tmp/work/prd"))
        self.assertEqual(prd.app_name, "My App")
        self.assertEqual(prd.ci, ("build-debug", "test"))

    def test_parse_min_sdk_must_be_ascii_digits(self):
        for raw in ("+26", "2_6", "\u0662\u0666", "26.0", "-1"):
            with self.subTest(raw=raw):
                self.assertIn("minSdk must be an integer", self.parse_error(f"minSdk = {raw}\n"))

    def test_parse_output_dir_expands_user(self):
        prd = starter_prd.parse_prd("```properties\noutputDir = ~/proj\n```", Path("/t/a"))
        self.assertEqual(prd.output_dir, Path(os.path.normpath(Path.home() / "proj")))

    def test_parse_class_prefix(self):
        prd = starter_prd.parse_prd("```properties\nclassPrefix = Moim\n```", Path("/t/a"))
        self.assertEqual(prd.class_prefix, "Moim")
        self.assertEqual(starter_prd.effective_prefix(prd), "Moim")
        self.assertEqual(starter_prd.effective_prefix(make_prd()), "Devsample")


class ValidateTest(unittest.TestCase):
    def test_valid_passes(self):
        self.assertEqual(starter_prd.validate(make_prd()), [])

    def test_validate_package_name_rejects_keyword_segment(self):
        errs = starter_prd.validate(make_prd(package_name="com.club.fun"))
        self.assertEqual(len(errs), 1)
        self.assertIn("fun", errs[0])

    def test_validate_package_name_rejects_example(self):
        self.assertTrue(starter_prd.validate(make_prd(package_name="com.example.app")))

    def test_validate_package_name_rejects_bad_format(self):
        for name in ["Com.club.app", "com.1club.app", "club", "com..app"]:
            with self.subTest(name=name):
                self.assertTrue(starter_prd.validate(make_prd(package_name=name)))

    def test_validate_requires_package_name(self):
        errs = starter_prd.validate(make_prd(package_name=""))
        self.assertEqual(
            errs,
            ["packageName is required: ask the user to choose one (see AGENTS.md)"],
        )

    def test_validate_rejects_unknown_nav_and_ci(self):
        errs = starter_prd.validate(make_prd(navigation="nav4", ci=("deploy",)))
        self.assertEqual(
            errs,
            [
                "navigation 'nav4' is unknown (expected one of: nav2, nav3)",
                "ci 'deploy' is unknown (expected: build-debug, build-release, test, ui-diff)",
            ],
        )

    def test_validate_class_prefix(self):
        for ok in ("Moim", "M", "Moim2", "MoimApp"):
            with self.subTest(ok=ok):
                self.assertEqual(starter_prd.validate(make_prd(class_prefix=ok)), [])
        for bad in ("moim", "Mo-im", "Mo im", "Mo_im", "2Moim", "\u00c4pp", "Starter", "StarterKit"):
            with self.subTest(bad=bad):
                errs = starter_prd.validate(make_prd(class_prefix=bad))
                self.assertEqual(len(errs), 1)
                self.assertIn("classPrefix", errs[0])

    def test_class_prefix_allows_starter_last_segment(self):
        errs = starter_prd.validate(make_prd(package_name="io.club.starter"))
        self.assertIn("set classPrefix", errs[0])
        self.assertEqual(
            starter_prd.validate(make_prd(package_name="io.club.starter", class_prefix="Moim")),
            [],
        )

    def test_validate_base_url_requires_https_and_trailing_slash(self):
        self.assertTrue(starter_prd.validate(make_prd(base_url="http://a.com/")))
        self.assertTrue(starter_prd.validate(make_prd(base_url="https://a.com")))
        self.assertEqual(starter_prd.validate(make_prd(base_url="https://a.com/")), [])


    def test_validate_min_sdk_range(self):
        for sdk in (23, 26, 36):
            with self.subTest(sdk=sdk):
                self.assertEqual(starter_prd.validate(make_prd(min_sdk=sdk)), [])
        for sdk in (0, 21, 22, 37, 99):
            with self.subTest(sdk=sdk):
                errs = starter_prd.validate(make_prd(min_sdk=sdk))
                self.assertEqual(len(errs), 1)
                self.assertIn("23", errs[0])
                self.assertIn("36", errs[0])

    def test_validate_rejects_class_prefix_starting_with_starter(self):
        for name in ("io.club.starter", "io.club.starterkit", "io.club.starter_kit"):
            with self.subTest(name=name):
                errs = starter_prd.validate(make_prd(package_name=name))
                self.assertEqual(len(errs), 1)
                self.assertIn("Starter", errs[0])
        self.assertEqual(starter_prd.validate(make_prd(package_name="io.club.mystarter")), [])

    def test_validate_rejects_platform_first_segment(self):
        for first in ("java", "javax", "kotlin", "android", "androidx"):
            with self.subTest(first=first):
                errs = starter_prd.validate(make_prd(package_name=f"{first}.club.app"))
                self.assertEqual(len(errs), 1)
                self.assertIn(first, errs[0])
        self.assertEqual(starter_prd.validate(make_prd(package_name="io.android.app")), [])

    def test_validate_base_url_is_parsed(self):
        ok = ("https://a.com/", "https://api.a.com/v1/", "https://a.com:8443/x/")
        bad = (
            "https:///", "https://a.com/x y/", "https://a.com/\"/", "https://a.com/\\/",
            "https://a.com/'/", "ftp://a.com/", "https:/a.com/", " https://a.com/",
            "https://a.com/\t/", "https://user@a.com/", "https://a.com/?q=1/",
            "https://a.com/#x/", "https://a.com:99999/", "https://a.com:abc/",
            "https://\u00e4.com/", "https://:443/", "https://a.com/\u00e4/",
        )
        for url in ok:
            with self.subTest(url=url):
                self.assertEqual(starter_prd.validate(make_prd(base_url=url)), [])
        for url in bad:
            with self.subTest(url=url):
                self.assertEqual(len(starter_prd.validate(make_prd(base_url=url))), 1)


class HelpersTest(unittest.TestCase):
    def test_class_prefix(self):
        self.assertEqual(generate.class_prefix("io.github.club.devsample"), "Devsample")
        self.assertEqual(generate.class_prefix("com.a.club_moim"), "ClubMoim")

    def test_app_name_is_escaped_for_android_strings(self):
        self.assertEqual(
            generate.escape_android_string("동아리's 앱 & \"모임\" @home"),
            "동아리\\'s 앱 &amp; \\\"모임\\\" @home",
        )
        self.assertEqual(generate.escape_android_string("@home"), "\\@home")
        self.assertEqual(generate.escape_android_string("?q"), "\\?q")
        self.assertEqual(generate.escape_android_string("a<b"), "a&lt;b")
        self.assertEqual(generate.escape_android_string("100% 모임"), "100% 모임")

    def test_find_leftovers_reports_file_and_line(self):
        with tempfile.TemporaryDirectory() as d:
            root = Path(d)
            (root / "a.kt").write_text("x\ny\nclass StarterFoo\n", encoding="utf-8")
            (root / "b.kts").write_text("verifyStarter\n", encoding="utf-8")
            self.assertEqual(generate.find_leftovers(root), ["a.kt:3"])
            (root / "a.kt").write_text("import com.example.starter.X\n", encoding="utf-8")
            self.assertEqual(generate.find_leftovers(root), ["a.kt:1"])
            (root / "a.kt").unlink()
            self.assertEqual(generate.find_leftovers(root), [])


    def test_find_leftovers_scans_all_utf8_files_and_skips_binary(self):
        with tempfile.TemporaryDirectory() as d:
            root = Path(d)
            (root / "gradlew").write_text("#!/bin/sh\n# Starter\n", encoding="utf-8")
            (root / "x.bin").write_bytes(b"\0Starter")
            (root / "y.dat").write_bytes(b"\xff\xfeStarter")
            self.assertEqual(generate.find_leftovers(root), ["gradlew:2"])

    def test_find_prefix_clashes_on_real_template(self):
        roots = [REPO / "template", REPO / "variants/navigation/nav3"]
        clashes = generate.find_prefix_clashes(roots, "Coroutine")
        self.assertTrue(clashes)
        self.assertTrue(
            all("import kotlinx.coroutines.CoroutineDispatcher clashes with "
                "CoroutineDispatcher (from StarterDispatcher)" in c for c in clashes),
            clashes,
        )
        self.assertTrue(clashes[0].startswith("template/"), clashes)
        for ok in ("Devsample", "Moim", "Robolectric"):
            with self.subTest(prefix=ok):
                self.assertEqual(generate.find_prefix_clashes(roots, ok), [])
        for bad in ("Room", "Material"):
            with self.subTest(prefix=bad):
                self.assertTrue(generate.find_prefix_clashes(roots, bad))

    def test_find_prefix_clashes_aliases_and_own_imports(self):
        with tempfile.TemporaryDirectory() as d:
            root = Path(d) / "template"
            root.mkdir()
            (root / "A.kt").write_text(
                "import x.y.CoDispatcher as XD\nimport com.example.starter.StarterDispatcher\n"
                "val a = StarterDispatcher\n", encoding="utf-8",
            )
            (root / "B.kt").write_text("import x.y.CoDispatcher\n", encoding="utf-8")
            self.assertEqual(generate.find_prefix_clashes([root], "Co"), [])
            (root / "C.kt").write_text(
                "import x.y.`CoDispatcher`\nval a = StarterDispatcher\n", encoding="utf-8"
            )
            self.assertEqual(
                generate.find_prefix_clashes([root], "Co"),
                ["template/C.kt: import x.y.CoDispatcher clashes with CoDispatcher "
                 "(from StarterDispatcher)"],
            )

    def test_find_leftovers_reports_path_names(self):
        with tempfile.TemporaryDirectory() as d:
            root = Path(d)
            sub = root / "schemas" / "com.example.starter.core.database.StarterDatabase"
            sub.mkdir(parents=True)
            (sub / "1.json").write_text("{}", encoding="utf-8")
            self.assertEqual(
                generate.find_leftovers(root),
                ["schemas/com.example.starter.core.database.StarterDatabase/1.json:0"],
            )


class SortImportsTest(unittest.TestCase):
    def test_sort_imports_moves_project_imports_into_lexicographic_position(self):
        src = (
            "package p\n\nimport androidx.compose.Foo\nimport io.github.club.devsample.X\n"
            "import com.github.takahirom.Y\nimport kotlin.test.Z\nimport java.io.File\n\nclass A\n"
        )
        want = (
            "package p\n\nimport androidx.compose.Foo\nimport com.github.takahirom.Y\n"
            "import io.github.club.devsample.X\nimport java.io.File\nimport kotlin.test.Z\n\nclass A\n"
        )
        self.assertEqual(want, generate.sort_kotlin_imports(src))

    def test_sort_imports_ascii_and_backticks(self):
        src = "import a.b.c\nimport a.b.C\nimport a.b.*\nimport a.b.`when`\nimport a.B.X\nimport a.b.Foo.Bar\nimport a.b.`Foo$Bar`\n"
        want = "import a.B.X\nimport a.b.*\nimport a.b.C\nimport a.b.`Foo$Bar`\nimport a.b.Foo.Bar\nimport a.b.c\nimport a.b.`when`\n"
        self.assertEqual(want, generate.sort_kotlin_imports(src))

    def test_sort_imports_aliases_last(self):
        src = "import z.Z as Y\nimport kotlin.A\nimport a.B as C\nimport java.A\nimport b.B\n"
        want = "import b.B\nimport java.A\nimport kotlin.A\nimport a.B as C\nimport z.Z as Y\n"
        self.assertEqual(want, generate.sort_kotlin_imports(src))

    def test_sort_imports_removes_blank_lines_and_duplicates(self):
        src = "package p\n\nimport b.B\n\nimport a.A\nimport b.B\n\nfun f() {}\n"
        want = "package p\n\nimport a.A\nimport b.B\n\nfun f() {}\n"
        self.assertEqual(want, generate.sort_kotlin_imports(src))

    def test_sort_imports_no_imports_unchanged(self):
        src = "package p\n\nclass A\n"
        self.assertEqual(src, generate.sort_kotlin_imports(src))

    def test_sort_imports_keeps_rest_of_file_identical(self):
        src = "// header\r\npackage p\r\n\r\nimport b.B\r\nimport a.A\r\n\r\n/** import x.Y */\r\nclass A  \r\n"
        want = "// header\r\npackage p\r\n\r\nimport a.A\r\nimport b.B\r\n\r\n/** import x.Y */\r\nclass A  \r\n"
        self.assertEqual(want, generate.sort_kotlin_imports(src))

    def test_sort_imports_ignores_raw_strings_and_header_comments(self):
        src = (
            "/*\nimport z.Z\nimport a.A\n*/\n@file:Suppress(\n    \"X\",\n)\n\npackage p\n\n"
            "import b.B\nimport a.A\n\nval s = \"\"\"\nimport z.Z\nimport a.A\n\"\"\"\n"
        )
        want = src.replace("import b.B\nimport a.A\n\nval", "import a.A\nimport b.B\n\nval")
        self.assertEqual(want, generate.sort_kotlin_imports(src))
        raw_only = "package p\n\nval s = \"\"\"\nimport z.Z\nimport a.A\n\"\"\"\n"
        self.assertEqual(raw_only, generate.sort_kotlin_imports(raw_only))

    def test_sort_imports_comment_splits_runs(self):
        src = "package p\n\nimport d.D\nimport c.C\n// keep\nimport b.B\n/* x\n*/\nimport z.Z\nimport a.A\n\nclass A\n"
        want = "package p\n\nimport c.C\nimport d.D\n// keep\nimport b.B\n/* x\n*/\nimport a.A\nimport z.Z\n\nclass A\n"
        self.assertEqual(want, generate.sort_kotlin_imports(src))

    def test_sort_imports_without_trailing_newline(self):
        self.assertEqual("import a.A\nimport b.B", generate.sort_kotlin_imports("import b.B\nimport a.A"))


class GenerateTest(unittest.TestCase):
    def setUp(self):
        self._tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self._tmp.cleanup)
        self.tmp = Path(self._tmp.name)
        self.repo = self.tmp / "repo"
        shutil.copytree(FIXTURE, self.repo)
        self.out = self.tmp / "out"

    def gen(self, overwrite=False, **kw):
        return generate.generate(
            make_prd(output_dir=self.out, **kw), self.repo, overwrite, git=False
        )

    def test_generate_renames_package_dirs_and_tokens(self):
        self.gen(app_name="동아리's 앱 & \"모임\"")
        app = self.out / "app/src/main/kotlin/io/github/club/devsample/DevsampleApp.kt"
        self.assertTrue(app.is_file())
        self.assertFalse((self.out / "app/src/main/kotlin/com").exists())
        self.assertIn("package io.github.club.devsample", app.read_text(encoding="utf-8"))
        self.assertIn("class DevsampleApp", app.read_text(encoding="utf-8"))
        self.assertIn("verifyStarter", (self.out / "build.gradle.kts").read_text())
        strings = (self.out / "app/src/main/res/values/strings.xml").read_text(encoding="utf-8")
        self.assertIn('<string name="app_name">동아리\\\'s 앱 &amp; \\"모임\\"</string>', strings)
        props = (self.out / "gradle.properties").read_text()
        self.assertIn("starter.minSdk=26", props)
        self.assertIn("starter.baseUrl=https://a.com/", props)

    def test_generate_renames_dotted_package_dir_names(self):
        schema = self.repo / "template/core/database/schemas/com.example.starter.core.database.StarterDatabase"
        schema.mkdir(parents=True)
        (schema / "1.json").write_text("{}", encoding="utf-8")
        self.gen()
        new = self.out / "core/database/schemas/io.github.club.devsample.core.database.DevsampleDatabase/1.json"
        self.assertTrue(new.is_file())
        self.assertEqual(list((self.out / "core/database/schemas").iterdir()), [new.parent])
        self.assertEqual(generate.find_leftovers(self.out), [])

    def test_generate_sorts_imports_in_output(self):
        src = self.repo / "template/app/src/main/kotlin/com/example/starter/Imp.kt"
        src.write_text(
            "package com.example.starter\n\nimport com.github.X\nimport com.example.starter.Y\n\nclass StarterImp\n",
            encoding="utf-8",
        )
        self.gen(package_name="org.zeta.app")
        out = (self.out / "app/src/main/kotlin/org/zeta/app/Imp.kt").read_text(encoding="utf-8")
        self.assertIn("import com.github.X\nimport org.zeta.app.Y\n", out)

    def test_gradle_properties_values_replaced_once(self):
        self.gen(min_sdk=30, base_url="https://b.io/api/")
        props = (self.out / "gradle.properties").read_text()
        self.assertEqual(
            props, "org.gradle.jvmargs=-Xmx2g\nstarter.minSdk=30\nstarter.baseUrl=https://b.io/api/\n"
        )

    def test_missing_or_duplicated_value_lines_are_template_errors(self):
        props = self.repo / "template/gradle.properties"
        props.write_text("starter.minSdk=26\n", encoding="utf-8")
        with self.assertRaisesRegex(ValueError, "template changed: expected exactly one starter.baseUrl"):
            self.gen()
        props.write_text("starter.minSdk=26\nstarter.minSdk=27\nstarter.baseUrl=x\n", encoding="utf-8")
        with self.assertRaisesRegex(ValueError, "found 2"):
            self.gen(overwrite=True)
        props.unlink()
        with self.assertRaisesRegex(ValueError, "template changed: gradle.properties is missing"):
            self.gen(overwrite=True)
        props.write_text("starter.minSdk=26\nstarter.baseUrl=x\n", encoding="utf-8")
        (self.repo / "template/app/src/main/res/values/strings.xml").write_text("<resources/>\n")
        with self.assertRaisesRegex(ValueError, "app_name"):
            self.gen(overwrite=True)

    def test_user_values_containing_template_tokens(self):
        self.gen(app_name="My Starter", base_url="https://com.example.starter.io/Starter/")
        strings = (self.out / "app/src/main/res/values/strings.xml").read_text(encoding="utf-8")
        self.assertIn(">My Starter<", strings)
        props = (self.out / "gradle.properties").read_text()
        self.assertIn("starter.baseUrl=https://com.example.starter.io/Starter/", props)

    def test_prefix_clash_fails_before_writing(self):
        dsp = self.repo / "template/app/src/main/kotlin/com/example/starter/Dsp.kt"
        dsp.write_text(
            "package com.example.starter\n\nimport kotlinx.coroutines.CoroutineDispatcher\n\n"
            "enum class StarterDispatcher\nfun f(d: CoroutineDispatcher) = StarterDispatcher\n",
            encoding="utf-8",
        )
        with self.assertRaises(ValueError) as cm:
            self.gen(package_name="io.club.coroutine")
        msg = str(cm.exception)
        self.assertIn("class prefix 'Coroutine' clashes", msg)
        self.assertIn("set classPrefix", msg)
        self.assertIn("kotlinx.coroutines.CoroutineDispatcher", msg)
        self.assertFalse(self.out.exists())
        self.gen(package_name="io.club.coroutine", class_prefix="Moim")
        out = self.out / "app/src/main/kotlin/io/club/coroutine/Dsp.kt"
        self.assertIn("enum class MoimDispatcher", out.read_text(encoding="utf-8"))
        self.assertTrue((self.out / "app/src/main/kotlin/io/club/coroutine/MoimApp.kt").is_file())

    def test_crlf_files_keep_crlf(self):
        src = self.repo / "template/app/src/main/kotlin/com/example/starter/Crlf.kt"
        src.write_bytes(b"package com.example.starter\r\n\r\nimport b.B\r\nimport a.A\r\n\r\nclass StarterX\r\n")
        self.gen()
        out = (self.out / "app/src/main/kotlin/io/github/club/devsample/Crlf.kt").read_bytes()
        self.assertEqual(
            out, b"package io.github.club.devsample\r\n\r\nimport a.A\r\nimport b.B\r\n\r\nclass DevsampleX\r\n"
        )

    def test_package_with_build_segment(self):
        self.gen(package_name="com.build.app")
        app = self.out / "app/src/main/kotlin/com/build/app/AppApp.kt"
        self.assertTrue(app.is_file())
        self.assertIn("package com.build.app", app.read_text(encoding="utf-8"))
        self.assertEqual(generate.find_leftovers(self.out), [])

    @unittest.skipIf(os.name == "nt", "symlinks need privileges on Windows")
    def test_refuses_symlink_into_repo_subdir(self):
        (self.repo / "docs").mkdir()
        link = self.tmp / "link"
        link.symlink_to(self.repo / "docs", target_is_directory=True)
        with self.assertRaisesRegex(ValueError, "inside the starter repo"):
            generate.generate(make_prd(output_dir=link / "o"), self.repo, False, git=False)
        self.assertFalse((self.repo / "docs/o").exists())
        (self.repo / "build").mkdir()
        blink = self.tmp / "blink"
        blink.symlink_to(self.repo / "build", target_is_directory=True)
        generate.generate(make_prd(output_dir=blink / "o"), self.repo, False, git=False)
        self.assertTrue((self.repo / "build/o/build.gradle.kts").is_file())

    def test_overwrite_removes_read_only_entries(self):
        self.gen()
        ro_dir = self.out / ".git" / "objects"
        ro_dir.mkdir(parents=True)
        ro = ro_dir / "pack"
        ro.write_text("x")
        ro.chmod(stat.S_IREAD)
        ro_dir.chmod(stat.S_IREAD | stat.S_IEXEC)
        self.addCleanup(lambda: ro_dir.exists() and ro_dir.chmod(stat.S_IRWXU))
        self.gen(overwrite=True)
        self.assertFalse((self.out / ".git").exists())
        self.assertTrue((self.out / generate.MARKER_NAME).is_file())

    def test_git_init_commands_and_order(self):
        (self.repo / "template/gradlew").write_text("#!/bin/sh\n")
        with mock.patch.object(generate.subprocess, "run") as run:
            generate.generate(make_prd(output_dir=self.out), self.repo, False, git=True)
        cmds = [c.args[0] for c in run.call_args_list]
        self.assertEqual(
            cmds,
            [
                ["git", "init", "-b", "main"],
                ["git", "add", "-A"],
                ["git", "update-index", "--chmod=+x", "gradlew"],
                ["git", "-c", "commit.gpgsign=false", "commit", "--no-verify",
                 "-m", "chore: initial project from android-project-starter"],
            ],
        )
        for c in run.call_args_list:
            self.assertEqual(c.kwargs["cwd"], self.out)
            self.assertEqual(c.kwargs["stdout"], subprocess.PIPE)
            self.assertTrue(c.kwargs["check"])

    def test_git_init_skips_chmod_without_gradlew(self):
        with mock.patch.object(generate.subprocess, "run") as run:
            generate.generate(make_prd(output_dir=self.out), self.repo, False, git=True)
        self.assertNotIn("update-index", [c.args[0][1] for c in run.call_args_list])

    def test_generate_applies_nav_variant_and_ci(self):
        self.gen()
        self.assertTrue((self.out / "x.kt").is_file())
        self.assertIn("nav3", (self.out / "x.kt").read_text())
        self.assertFalse((self.out / ".github").exists())

        self.gen(overwrite=True, ci=("test",))
        wf = self.out / ".github/workflows"
        self.assertTrue((wf / "shared.yml").is_file())
        self.assertTrue((wf / "test.yml").is_file())
        self.assertFalse((wf / "ui.yml").exists())

    def test_refuses_non_empty_output_dir(self):
        self.out.mkdir()
        (self.out / "keep.txt").write_text("hi")
        with self.assertRaises(FileExistsError):
            self.gen()
        self.assertTrue((self.out / "keep.txt").exists())

    def test_generate_skips_build_outputs(self):
        t = self.repo / "template"
        for d in ("build", ".gradle", ".idea"):
            (t / d).mkdir()
            (t / d / "junk.txt").write_text("x")
        (t / "local.properties").write_text("sdk.dir=/x")
        self.gen()
        for n in ("build", ".gradle", ".idea", "local.properties"):
            self.assertFalse((self.out / n).exists(), n)

    def test_refuses_dangerous_output_dir(self):
        with self.assertRaises(ValueError):
            generate.generate(make_prd(output_dir=self.repo), self.repo, True, git=False)

    def test_package_segment_named_like_skipped_dir(self):
        self.gen(package_name="io.club.build.app")
        app = self.out / "app/src/main/kotlin/io/club/build/app/AppApp.kt"
        self.assertTrue(app.is_file())
        self.assertIn("package io.club.build.app", app.read_text(encoding="utf-8"))
        (app.parent / "Bad.kt").write_text("class StarterBad\n")
        self.assertEqual(
            generate.find_leftovers(self.out),
            ["app/src/main/kotlin/io/club/build/app/Bad.kt:1"],
        )

    def test_refuses_output_inside_template_variants_scripts(self):
        for d in ("template", "variants/navigation", "scripts"):
            with self.subTest(d=d):
                (self.repo / d).mkdir(parents=True, exist_ok=True)
                with self.assertRaises(ValueError):
                    generate.generate(
                        make_prd(output_dir=self.repo / d / "o"), self.repo, True, git=False
                    )
        self.assertTrue((self.repo / "template/build.gradle.kts").exists())


    def test_overwrite_refuses_dir_without_marker(self):
        self.out.mkdir()
        (self.out / "keep.txt").write_text("hi")
        with self.assertRaises(ValueError) as cm:
            self.gen(overwrite=True)
        self.assertIn("not created by generate.py", str(cm.exception))
        self.assertIn(str(self.out), str(cm.exception))
        self.assertTrue((self.out / "keep.txt").exists())

    def test_overwrite_replaces_dir_with_marker(self):
        self.gen()
        marker = self.out / generate.MARKER_NAME
        self.assertTrue(marker.is_file())
        self.assertEqual(generate.find_leftovers(self.out), [])
        (self.out / "stale.txt").write_text("x")
        self.gen(overwrite=True)
        self.assertFalse((self.out / "stale.txt").exists())
        self.assertTrue(marker.is_file())

    def test_overwrite_refuses_plain_file(self):
        self.out.write_text("not a dir")
        with self.assertRaises(ValueError):
            self.gen(overwrite=True)
        self.assertEqual(self.out.read_text(), "not a dir")

    def test_refuses_output_inside_repo_outside_build(self):
        for d in ("docs", ".git", "docs/sub", "build"):
            with self.subTest(d=d):
                (self.repo / d).mkdir(parents=True, exist_ok=True)
                with self.assertRaises(ValueError):
                    generate.generate(
                        make_prd(output_dir=self.repo / d / "o" if d != "build" else self.repo / d),
                        self.repo, True, git=False,
                    )
        self.assertFalse((self.repo / "docs/o").exists())

    def test_allows_output_under_repo_build(self):
        out = self.repo / "build/dev/x"
        generate.generate(make_prd(output_dir=out), self.repo, False, git=False)
        self.assertTrue((out / "build.gradle.kts").is_file())
        generate.generate(make_prd(output_dir=out), self.repo, True, git=False)
        self.assertTrue((out / generate.MARKER_NAME).is_file())

    def test_refuses_case_variant_of_protected_dir(self):
        case_repo = self.tmp / "REPO"
        if not case_repo.exists():
            self.skipTest("file system is case-sensitive")
        (self.repo / "template" / "keep.txt").write_text("x")
        for out in (case_repo / "TEMPLATE", case_repo / "Template" / "o", self.tmp / "Repo"):
            with self.subTest(out=out):
                with self.assertRaises(ValueError):
                    generate.generate(make_prd(output_dir=out), self.repo, True, git=False)
        self.assertTrue((self.repo / "template" / "keep.txt").exists())


class TemplateFilesTest(unittest.TestCase):
    def test_files_not_rewritten_contain_no_tokens(self):
        # generate.py rewrites only TEXT_EXTENSIONS; anything else must be token-free.
        token = re.compile(rb"com\.example\.starter|com/example/starter|\bStarter")
        found = []
        for root in (REPO / "template", REPO / "variants"):
            for p in generate._iter_files(root, skip=True):
                if not generate._is_text(p) and token.search(p.read_bytes()):
                    found.append(p.relative_to(REPO).as_posix())
        self.assertEqual(found, [])

    def test_app_name_is_not_formatted(self):
        # formatted="false" lets an appName contain a literal '%'.
        text = (REPO / "template/app/src/main/res/values/strings.xml").read_text(encoding="utf-8")
        self.assertIn('<string name="app_name" formatted="false">', text)


class CliTest(unittest.TestCase):
    def setUp(self):
        self._tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self._tmp.cleanup)
        self.tmp = Path(self._tmp.name)
        self.repo = self.tmp / "repo"
        shutil.copytree(FIXTURE, self.repo)

    def run_prd(self, text):
        prd = self.tmp / "p.prd.md"
        prd.write_text(text, encoding="utf-8")
        import contextlib
        import io
        with contextlib.redirect_stderr(io.StringIO()), contextlib.redirect_stdout(io.StringIO()):
            return generate.main(["--prd", str(prd), "--overwrite"], repo_root=self.repo)

    BODY = (
        "```properties\nappName = A\npackageName = io.club.app\n"
        "baseUrl = https://a.com/\nnavigation = nav3\noutputDir = out\n%s```\n"
    )

    def test_exit_codes(self):
        self.assertEqual(self.run_prd("no block"), 2)
        self.assertEqual(self.run_prd(self.BODY % "minSdk = abc\n"), 2)
        self.assertEqual(self.run_prd(self.BODY % "packageName = Bad\n"), 2)

    def test_exit_3_on_leftovers(self):
        # Real leftovers cannot survive replacement, so stub the scanner.
        orig = generate.find_leftovers
        generate.find_leftovers = lambda root: ["a.kt:1"]
        self.addCleanup(setattr, generate, "find_leftovers", orig)
        self.assertEqual(self.run_prd(self.BODY % ""), 3)

    def test_git_failure_is_warning_exit_0(self):
        import contextlib
        import io
        import subprocess
        from unittest import mock
        prd = self.tmp / "p.prd.md"
        prd.write_text(self.BODY % "", encoding="utf-8")
        err = io.StringIO()
        with mock.patch.object(generate.subprocess, "run", side_effect=FileNotFoundError("git")), \
                contextlib.redirect_stderr(err), contextlib.redirect_stdout(io.StringIO()):
            code = generate.main(["--prd", str(prd), "--overwrite"], repo_root=self.repo)
        self.assertEqual(code, 0)
        self.assertIn("warning: git init/commit failed", err.getvalue())
        err = io.StringIO()
        boom = subprocess.CalledProcessError(128, "git")
        with mock.patch.object(generate.subprocess, "run", side_effect=boom), \
                contextlib.redirect_stderr(err), contextlib.redirect_stdout(io.StringIO()):
            code = generate.main(["--prd", str(prd), "--overwrite"], repo_root=self.repo)
        self.assertEqual(code, 0)
        self.assertIn("warning: git init/commit failed", err.getvalue())

    def test_bom_crlf_prd(self):
        prd = self.tmp / "p.prd.md"
        prd.write_bytes(b"\xef\xbb\xbf" + (self.BODY % "").replace("\n", "\r\n").encode())
        with contextlib.redirect_stderr(io.StringIO()), contextlib.redirect_stdout(io.StringIO()):
            code = generate.main(["--prd", str(prd)], repo_root=self.repo)
        self.assertEqual(code, 0)
        self.assertTrue((self.tmp / "out/app/src/main/kotlin/io/club/app/AppApp.kt").is_file())

    def test_unknown_key_and_clash_exit_2(self):
        err = io.StringIO()
        with contextlib.redirect_stderr(err), contextlib.redirect_stdout(io.StringIO()):
            code = generate.main(["--prd", str(self._write(self.BODY % "colour = red\n"))],
                                 repo_root=self.repo)
        self.assertEqual(code, 2)
        self.assertIn("error: PRD line 7: unknown key 'colour'", err.getvalue())

    def test_real_template_coroutine_clash_and_class_prefix(self):
        body = self.BODY.replace("io.club.app", "io.club.coroutine")
        err = io.StringIO()
        with contextlib.redirect_stderr(err), contextlib.redirect_stdout(io.StringIO()):
            code = generate.main(["--prd", str(self._write(body % ""))], repo_root=REPO)
        self.assertEqual(code, 2)
        self.assertIn("class prefix 'Coroutine' clashes", err.getvalue())
        self.assertFalse((self.tmp / "out").exists())
        prd = starter_prd.parse_prd(body % "classPrefix = Moim\n", self.tmp)
        self.assertEqual(starter_prd.validate(prd), [])
        out = generate.generate(prd, REPO, False, git=False)
        self.assertTrue(
            (out / "core/common/src/main/kotlin/io/club/coroutine/core/common/Dispatcher.kt").is_file()
        )
        self.assertEqual(generate.find_leftovers(out), [])

    def _write(self, text):
        prd = self.tmp / "p.prd.md"
        prd.write_text(text, encoding="utf-8")
        return prd

    def test_oserror_during_copy_exit_2(self):
        from unittest import mock
        with mock.patch.object(generate.shutil, "copy2", side_effect=PermissionError("denied")):
            self.assertEqual(self.run_prd(self.BODY % ""), 2)


if __name__ == "__main__":
    unittest.main()
