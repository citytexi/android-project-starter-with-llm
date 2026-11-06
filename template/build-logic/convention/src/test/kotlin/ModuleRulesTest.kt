import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ModuleRulesTest {
    @Test
    fun `feature can depend on domain and designsystem`() {
        assertTrue(ModuleRules.isAllowed(":feature:sample", ":domain"))
        assertTrue(ModuleRules.isAllowed(":feature:sample", ":core:designsystem"))
        assertTrue(ModuleRules.isAllowed(":feature:sample", ":core:ui"))
        assertTrue(ModuleRules.isAllowed(":feature:sample", ":core:navigation"))
        assertTrue(ModuleRules.isAllowed(":feature:sample", ":core:common"))
    }

    @Test
    fun `feature must not depend on data, network, database, datastore`() {
        assertFalse(ModuleRules.isAllowed(":feature:sample", ":data"))
        assertFalse(ModuleRules.isAllowed(":feature:sample", ":core:network"))
        assertFalse(ModuleRules.isAllowed(":feature:sample", ":core:database"))
        assertFalse(ModuleRules.isAllowed(":feature:sample", ":core:datastore"))
    }

    @Test
    fun `feature must not depend on another feature or test helpers`() {
        assertFalse(ModuleRules.isAllowed(":feature:sample", ":feature:other"))
        assertFalse(ModuleRules.isAllowed(":feature:sample", ":core:testing:jvm"))
    }

    @Test
    fun `domain may only depend on core common`() {
        assertTrue(ModuleRules.isAllowed(":domain", ":core:common"))
        assertFalse(ModuleRules.isAllowed(":domain", ":core:ui"))
        assertFalse(ModuleRules.isAllowed(":domain", ":data"))
    }

    @Test
    fun `data may depend on domain and storage cores`() {
        assertTrue(ModuleRules.isAllowed(":data", ":domain"))
        assertTrue(ModuleRules.isAllowed(":data", ":core:network"))
        assertTrue(ModuleRules.isAllowed(":data", ":core:database"))
        assertTrue(ModuleRules.isAllowed(":data", ":core:datastore"))
        assertTrue(ModuleRules.isAllowed(":data", ":core:common"))
        assertFalse(ModuleRules.isAllowed(":data", ":core:ui"))
        assertFalse(ModuleRules.isAllowed(":data", ":feature:sample"))
    }

    @Test
    fun `core module must not depend on feature, data, domain`() {
        assertFalse(ModuleRules.isAllowed(":core:ui", ":feature:sample"))
        assertFalse(ModuleRules.isAllowed(":core:network", ":data"))
        assertFalse(ModuleRules.isAllowed(":core:database", ":domain"))
        assertTrue(ModuleRules.isAllowed(":core:ui", ":core:designsystem"))
    }

    @Test
    fun `ui cores may depend only on ui cores and common`() {
        val uiCores = listOf(":core:designsystem", ":core:ui", ":core:navigation")
        uiCores.forEach { from ->
            (uiCores + ":core:common").forEach { to -> assertTrue("$from -> $to", ModuleRules.isAllowed(from, to)) }
            listOf(":core:network", ":core:database", ":core:datastore", ":core:testing:jvm").forEach { to ->
                assertFalse("$from -> $to", ModuleRules.isAllowed(from, to))
            }
        }
    }

    @Test
    fun `non ui cores may depend on other cores`() {
        assertTrue(ModuleRules.isAllowed(":core:datastore", ":core:common"))
        assertTrue(ModuleRules.isAllowed(":core:network", ":core:ui"))
    }

    @Test
    fun `test configurations are exempt`() {
        listOf(
            "testImplementation",
            "testApi",
            "testCompileOnly",
            "testRuntimeOnly",
            "testDebugImplementation",
            "androidTestImplementation",
            "androidTestDebugImplementation",
            "testFixturesImplementation",
            "testFixturesApi",
            "debugUnitTestCompileClasspath",
            "debugAndroidTestRuntimeClasspath",
            "kspTest",
            "kspAndroidTest",
            "kotlinCompilerPluginClasspathTest",
            "screenshotTestImplementation",
            "integrationTests",
        ).forEach { assertTrue(it, ModuleRules.isTestConfiguration(it)) }
    }

    @Test
    fun `main and custom configurations are checked`() {
        listOf(
            "implementation",
            "api",
            "compileOnly",
            "runtimeOnly",
            "debugImplementation",
            "releaseApi",
            "ksp",
            "debugCompileClasspath",
            "releaseRuntimeClasspath",
            "latestImplementation",
            "contestApi",
            "testerImplementation",
            "extra",
        ).forEach { assertFalse(it, ModuleRules.isTestConfiguration(it)) }
    }

    @Test
    fun `core testing modules are only for test configurations`() {
        assertFalse(ModuleRules.isAllowed(":core:datastore", ":core:testing:android"))
        assertTrue(ModuleRules.isAllowed(":core:testing:android", ":core:testing:jvm"))
        assertTrue(ModuleRules.isAllowed(":core:testing:android", ":core:datastore"))
    }

    @Test
    fun `app can depend on anything`() {
        listOf(":feature:sample", ":domain", ":data", ":core:network", ":core:datastore").forEach {
            assertTrue(ModuleRules.isAllowed(":app", it))
        }
    }

    @Test
    fun `konsist depends on nothing`() {
        assertFalse(ModuleRules.isAllowed(":konsist", ":domain"))
    }
}
