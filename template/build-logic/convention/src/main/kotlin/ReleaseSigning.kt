import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Project
import java.io.StringReader
import java.util.Properties

private const val KEYSTORE_PATH = "STARTER_KEYSTORE_PATH"
private const val KEYSTORE_PASSWORD = "STARTER_KEYSTORE_PASSWORD"
private const val KEY_ALIAS = "STARTER_KEY_ALIAS"
private const val KEY_PASSWORD = "STARTER_KEY_PASSWORD"

private const val DEBUG_SIGNING_WARNING = "⚠ release is signed with the DEBUG key. Do not upload this build."

/**
 * Signs `release` with the keystore named by `local.properties` or environment variables
 * ([KEYSTORE_PATH], [KEYSTORE_PASSWORD], [KEY_ALIAS], [KEY_PASSWORD]; local.properties wins).
 * If any of the four is missing, release falls back to the debug key and the build warns.
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

    val storePath = value(KEYSTORE_PATH)
    val storePassword = value(KEYSTORE_PASSWORD)
    val alias = value(KEY_ALIAS)
    val keyPassword = value(KEY_PASSWORD)

    val release = extension.buildTypes.getByName("release")
    if (storePath != null && storePassword != null && alias != null && keyPassword != null) {
        release.signingConfig =
            extension.signingConfigs.create("release") {
                storeFile = rootProject.file(storePath)
                this.storePassword = storePassword
                keyAlias = alias
                this.keyPassword = keyPassword
            }
    } else {
        logger.warn(DEBUG_SIGNING_WARNING)
        release.signingConfig = extension.signingConfigs.getByName("debug")
    }
}
