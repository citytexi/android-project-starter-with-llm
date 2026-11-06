import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType

/** `starter.android.compose`: Compose compiler + BOM. Apply after an Android plugin. */
class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply(libs.plugins.kotlin.compose.get().pluginId)
        val application = extensions.findByType<ApplicationExtension>()
        val library = extensions.findByType<LibraryExtension>()
        when {
            application != null -> application.buildFeatures.compose = true
            library != null -> library.buildFeatures.compose = true
            else -> error("starter.android.compose needs starter.android.application or starter.android.library first")
        }
        dependencies {
            val bom = platform(libs.androidx.compose.bom)
            implementation(bom)
            implementation(libs.androidx.compose.ui)
            implementation(libs.androidx.compose.ui.graphics)
            implementation(libs.androidx.compose.ui.tooling.preview)
            implementation(libs.androidx.compose.material3)
            debugImplementation(libs.androidx.compose.ui.tooling)

            androidTestImplementation(bom)
            androidTestImplementation(libs.androidx.compose.ui.test.junit4)
            debugImplementation(libs.androidx.compose.ui.test.manifest)
        }
    }
}
