/**
 * Module dependency allow-table, enforced at the Gradle level.
 *
 * Every configuration is checked except test configurations ([isTestConfiguration]), which is
 * how `:core:testing:*` stays usable from anywhere in tests.
 *
 * - `:app` may depend on anything.
 * - `:feature:*` may depend on `:domain`, `:core:designsystem`, `:core:ui`, `:core:navigation`, `:core:common`.
 *   `:core:common` goes beyond the design spec's list on purpose: `:domain` already exposes it as
 *   `api`, and it holds the dispatcher qualifiers a ViewModel may inject.
 * - `:domain` may depend on `:core:common`.
 * - `:data` may depend on `:domain`, `:core:network`, `:core:database`, `:core:datastore`, `:core:common`.
 * - `:core:designsystem`, `:core:ui`, `:core:navigation` (UI cores) may depend only on each other
 *   and on `:core:common`.
 * - Other `:core:*` modules may depend on other `:core:*` modules. `:core:testing:*` is test-only,
 *   so only other `:core:testing:*` modules may use it from main configurations.
 * - `:konsist` depends on nothing (it reads source paths).
 * - Anything else is denied.
 */
object ModuleRules {
    private val FEATURE_ALLOWED = setOf(
        ":domain",
        ":core:designsystem",
        ":core:ui",
        ":core:navigation",
        ":core:common",
    )
    private val DOMAIN_ALLOWED = setOf(":core:common")
    private val UI_CORES = setOf(":core:designsystem", ":core:ui", ":core:navigation")
    private val UI_CORE_ALLOWED = UI_CORES + ":core:common"
    private val DATA_ALLOWED = setOf(
        ":domain",
        ":core:network",
        ":core:database",
        ":core:datastore",
        ":core:common",
    )

    fun isAllowed(from: String, to: String): Boolean = when {
        from == to -> true
        from == ":app" -> true
        from.isUnder(":feature") -> to in FEATURE_ALLOWED
        from == ":domain" -> to in DOMAIN_ALLOWED
        from == ":data" -> to in DATA_ALLOWED
        from.isUnder(":core:testing") -> to.isUnder(":core")
        from in UI_CORES -> to in UI_CORE_ALLOWED
        from.isUnder(":core") -> to.isUnder(":core") && !to.isUnder(":core:testing")
        else -> false
    }

    /** Name words that mark a test configuration: `test`, `Test`, `tests`, `Tests`. */
    private val TEST_WORDS = setOf("test", "tests")

    /**
     * True for configurations that only feed tests: `testImplementation`, `androidTestApi`,
     * `testFixturesImplementation`, `debugUnitTestCompileClasspath`, `kspTest`,
     * `testDebugImplementation`, ... The name is split into camel-case words and one word must be
     * exactly `test`/`Test`(`s`). `latestImplementation` or `contestApi` are therefore checked.
     */
    fun isTestConfiguration(name: String): Boolean =
        name
            .split(Regex("(?<=[a-z0-9])(?=[A-Z])|[^A-Za-z0-9]+"))
            .any { it.lowercase() in TEST_WORDS }

    private fun String.isUnder(parent: String): Boolean = startsWith("$parent:")
}
