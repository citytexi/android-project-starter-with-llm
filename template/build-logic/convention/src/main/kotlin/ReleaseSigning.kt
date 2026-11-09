import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.GradleException
import org.gradle.api.Project
import java.io.StringReader
import java.util.Properties

private const val KEYSTORE_PATH = "STARTER_KEYSTORE_PATH"
private const val KEYSTORE_PASSWORD = "STARTER_KEYSTORE_PASSWORD"
private const val KEY_ALIAS = "STARTER_KEY_ALIAS"
private const val KEY_PASSWORD = "STARTER_KEY_PASSWORD"

private const val DEBUG_SIGNING_WARNING = "⚠ release is signed with the DEBUG key. Do not upload this build."

/** Lifecycle tasks that produce a release artifact. The warning is attached to these only. */
private val RELEASE_ARTIFACT_TASKS = setOf("assembleRelease", "bundleRelease")

/**
 * Signs `release` with the keystore named by `local.properties` or environment variables
 * ([KEYSTORE_PATH], [KEYSTORE_PASSWORD], [KEY_ALIAS], [KEY_PASSWORD]; local.properties wins).
 *
 * - All four set: release uses that keystore.
 * - None set: release falls back to the debug key, and `assembleRelease`/`bundleRelease` print
 *   a warning when they run. Builds that do not produce a release artifact stay quiet.
 * - Only some set: the build fails and names the missing keys (never the values).
 */
internal fun Project.configureReleaseSigning(extension: ApplicationExtension) {
    val localProperties = Properties()
    providers
        .fileContents(rootProject.layout.projectDirectory.file("local.properties"))
        .asText
        .orNull
        ?.let { localProperties.load(StringReader(it)) }

    fun value(name: String): String? =
        localProperties.getProperty(name)?.takeIf { it.isNotBlank() }
            ?: providers.environmentVariable(name).orNull?.takeIf { it.isNotBlank() }

    val values = listOf(KEYSTORE_PATH, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD).associateWith { value(it) }
    val missing = values.filterValues { it == null }.keys

    val release = extension.buildTypes.getByName("release")
    when {
        missing.isEmpty() ->
            release.signingConfig =
                extension.signingConfigs.create("release") {
                    storeFile = rootProject.file(values.getValue(KEYSTORE_PATH)!!)
                    storePassword = values.getValue(KEYSTORE_PASSWORD)
                    keyAlias = values.getValue(KEY_ALIAS)
                    keyPassword = values.getValue(KEY_PASSWORD)
                }
        missing.size == values.size -> {
            release.signingConfig = extension.signingConfigs.getByName("debug")
            // A task action, not a configuration-time log: it prints only when a release artifact
            // is built, and it survives configuration-cache reuse. Lifecycle tasks have no outputs,
            // so the action runs on every such build, cached or not.
            tasks
                .configureEach {
                    if (name in RELEASE_ARTIFACT_TASKS) doFirst { logger.warn(DEBUG_SIGNING_WARNING) }
                }
        }
        else ->
            throw GradleException(
                "Release signing is partly configured. Missing: ${missing.joinToString(", ")}. " +
                    "Set all four keys in local.properties or the environment, or none of them to sign release with the debug key.",
            )
    }
}
