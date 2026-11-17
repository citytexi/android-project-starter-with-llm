private val ROBORAZZI_RUN_TASK = Regex("^(record|compare|verify|verifyAndRecord)Roborazzi[A-Z]\\w*$")

/**
 * Whether [taskName] records or compares Roborazzi screenshots, such as `recordRoborazziDebug`.
 *
 * Checked against the tasks in the execution graph, not the command line: `verifyStarter` pulls in
 * `recordRoborazziDebug` without naming it, and must render the generated preview tests too.
 */
internal fun isRoborazziRunTask(taskName: String): Boolean = ROBORAZZI_RUN_TASK.matches(taskName)
