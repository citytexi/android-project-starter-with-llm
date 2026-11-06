/**
 * Module dependency allow-table, enforced at the Gradle level.
 *
 * Only main configurations (`implementation`, `api`, ...) are checked. Test configurations
 * are exempt, which is how `:core:testing:*` stays usable from anywhere in tests.
 *
 * - `:app` may depend on anything.
 * - `:feature:*` may depend on `:domain`, `:core:designsystem`, `:core:ui`, `:core:navigation`, `:core:common`.
 * - `:domain` may depend on `:core:common`.
 * - `:data` may depend on `:domain`, `:core:network`, `:core:database`, `:core:datastore`, `:core:common`.
 * - `:core:*` may depend on other `:core:*` modules. `:core:testing:*` is test-only, so only other
 *   `:core:testing:*` modules may use it from main configurations.
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
        from.isUnder(":core") -> to.isUnder(":core") && !to.isUnder(":core:testing")
        else -> false
    }

    private fun String.isUnder(parent: String): Boolean = startsWith("$parent:")
}
