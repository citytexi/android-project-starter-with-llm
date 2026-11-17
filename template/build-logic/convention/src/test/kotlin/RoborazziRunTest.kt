import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RoborazziRunTest {
    @Test
    fun `record, compare and verify tasks are Roborazzi runs`() {
        listOf("recordRoborazziDebug", "compareRoborazziDebug", "verifyRoborazziDebug", "verifyAndRecordRoborazziDebug")
            .forEach { assertTrue(it, isRoborazziRunTask(it)) }
    }

    @Test
    fun `other tasks are not Roborazzi runs`() {
        listOf("testDebugUnitTest", "clearRoborazziDebug", "verifyStarter", "testStarter", "recordRoborazzi", "assembleDebug")
            .forEach { assertFalse(it, isRoborazziRunTask(it)) }
    }
}
