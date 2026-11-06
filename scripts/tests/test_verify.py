import io
import os
import stat
import sys
import tempfile
import unittest
from contextlib import redirect_stderr, redirect_stdout
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

import verify  # noqa: E402


def write(root, rel, text):
    p = Path(root) / rel
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(text, encoding="utf-8")


class SettingsModulesTest(unittest.TestCase):
    def test_settings_modules_ignores_whitespace_and_comments(self):
        text = 'include(\n  ":app", // x\n  ":core:testing:jvm",\n)\n// include(":old")\n'
        self.assertEqual(verify.settings_modules(text), [":app", ":core:testing:jvm"])

    def test_multiple_args_calls_and_block_comments(self):
        text = (
            'rootProject.name = "X"\n'
            'include(":b:c", ":a")\n'
            '/* include(":gone")\n include(":gone2") */\n'
            'include(":d")\n'
        )
        self.assertEqual(verify.settings_modules(text), [":a", ":b:c", ":d"])


class CompareStructureTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        self.a = Path(self.tmp.name) / "proj"
        self.b = Path(self.tmp.name) / "tpl"

    def fill(self, root, modules, catalog):
        inc = ", ".join(f'"{m}"' for m in modules)
        write(root, "settings.gradle.kts", f'rootProject.name = "n"\ninclude({inc})\n')
        write(root, "gradle/libs.versions.toml", catalog)

    def test_compare_structure_ok(self):
        self.fill(self.a, [":app"], "x=1\n")
        self.fill(self.b, [":app"], "x=1\n")
        self.assertEqual(verify.compare_structure(self.a, self.b), [])

    def test_compare_structure_reports_extra_module_and_catalog_diff(self):
        self.fill(self.a, [":app", ":feature:extra"], "x=2\n")
        self.fill(self.b, [":app"], "x=1\n")
        msgs = verify.compare_structure(self.a, self.b)
        self.assertEqual(len(msgs), 2)
        self.assertIn(":feature:extra", msgs[0])
        self.assertIn("libs.versions.toml", msgs[1])

    def test_missing_files_reported(self):
        self.a.mkdir()
        self.fill(self.b, [":app"], "x=1\n")
        msgs = verify.compare_structure(self.a, self.b)
        self.assertEqual(len(msgs), 2)


class GradleCommandTest(unittest.TestCase):
    def test_gradle_command_on_windows(self):
        p = Path("proj")
        self.assertEqual(
            verify.gradle_command("win32", p),
            [str(p / "gradlew.bat"), "verifyStarter", "--continue"],
        )

    def test_gradle_command_on_posix(self):
        p = Path("proj")
        self.assertEqual(
            verify.gradle_command("linux", p),
            [str(p / "gradlew"), "verifyStarter", "--continue"],
        )


class MainTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        self.root = Path(self.tmp.name)
        for d in ("proj", "template"):
            write(self.root / d, "settings.gradle.kts", 'include(":app")\n')
            write(self.root / d, "gradle/libs.versions.toml", "x=1\n")

    def run_main(self, project):
        out, err = io.StringIO(), io.StringIO()
        with redirect_stdout(out), redirect_stderr(err):
            code = verify.main([str(project)], repo_root=self.root)
        return code, out.getvalue(), err.getvalue()

    def test_missing_project_exits_4(self):
        code, _, err = self.run_main(self.root / "nope")
        self.assertEqual(code, 4)
        self.assertIn("error:", err)

    def test_structure_mismatch_exits_4_and_skips_gradle(self):
        write(self.root / "proj", "gradle/libs.versions.toml", "x=2\n")
        code, _, err = self.run_main(self.root / "proj")
        self.assertEqual(code, 4)
        self.assertIn("libs.versions.toml", err)

    @unittest.skipIf(os.name == "nt", "posix shell script")
    def test_gradle_pass_and_fail(self):
        gw = self.root / "proj" / "gradlew"
        gw.write_text("#!/bin/sh\necho hello\nexit 0\n")
        code, _, _ = self.run_main(self.root / "proj")  # not executable: via sh
        self.assertEqual(code, 0)
        gw.write_text("#!/bin/sh\nfor i in $(seq 1 100); do echo line$i; done\nexit 1\n")
        gw.chmod(gw.stat().st_mode | stat.S_IXUSR)
        code, out, err = self.run_main(self.root / "proj")
        self.assertEqual(code, 5)
        shown = out + err
        self.assertIn("line100", shown)
        self.assertIn("line21", shown)
        self.assertNotIn("line20\n", shown)


if __name__ == "__main__":
    unittest.main()
