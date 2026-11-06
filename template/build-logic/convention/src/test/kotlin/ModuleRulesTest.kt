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
