import shutil
import sys
import tempfile
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

import generate  # noqa: E402
import starter_prd  # noqa: E402
from starter_prd import Prd  # noqa: E402

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
        self.assertEqual(len(errs), 2)

    def test_validate_base_url_requires_https_and_trailing_slash(self):
        self.assertTrue(starter_prd.validate(make_prd(base_url="http://a.com/")))
        self.assertTrue(starter_prd.validate(make_prd(base_url="https://a.com")))
        self.assertEqual(starter_prd.validate(make_prd(base_url="https://a.com/")), [])


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

    def test_gradle_properties_replaced_and_missing_ok(self):
        self.gen(min_sdk=30, base_url="https://b.io/api/")
        props = (self.out / "gradle.properties").read_text()
        self.assertIn("starter.minSdk=30", props)
        self.assertIn("starter.baseUrl=https://b.io/api/", props)
        (self.repo / "template/gradle.properties").unlink()
        self.gen(overwrite=True)
        self.assertFalse((self.out / "gradle.properties").exists())

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
        self.gen(overwrite=True)
        self.assertFalse((self.out / "keep.txt").exists())
        self.assertTrue((self.out / "build.gradle.kts").exists())

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


if __name__ == "__main__":
    unittest.main()
