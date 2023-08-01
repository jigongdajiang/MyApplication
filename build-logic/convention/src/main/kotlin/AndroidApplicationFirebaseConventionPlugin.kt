import com.android.build.api.dsl.ApplicationExtension
import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

/**
 * app module fireBase引入
 * Google 提供的一个平台，用于帮助开发者构建高质量的移动应用程序。
 * Firebase 提供了一系列的工具和服务，包括实时数据库、云存储、身份认证、
 * 消息推送、应用性能监控、测试和分析等功能。通过使用 Firebase，
 * 开发者可以更加轻松地开发、测试和部署应用程序，并且能够提供更好的用户体验。
 * 它还提供了强大的后端基础设施，使开发者能够专注于应用程序的功能和用户界面，而无需担心服务器端的管理和维护。
 *
 * com.google.gms.google-services是 Google 提供的一个Gradle插件，用于简化在 Android 应用程序中集成
 * Firebase 和 Google Play服务的过程。它主要用于在应用程序的build.gradle文件中配置和管理与 Firebase 和 Google Play服务相关的依赖项和设置。
 *
 * 在国内用不了。。。
 */
class AndroidApplicationFirebaseConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.google.gms.google-services")
                apply("com.google.firebase.firebase-perf")
                apply("com.google.firebase.crashlytics")
            }

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
            dependencies {
                val bom = libs.findLibrary("firebase-bom").get()
                add("implementation", platform(bom))
                "implementation"(libs.findLibrary("firebase.analytics").get())
                "implementation"(libs.findLibrary("firebase.performance").get())
                "implementation"(libs.findLibrary("firebase.crashlytics").get())
            }

            extensions.configure<ApplicationExtension> {
                buildTypes.configureEach {
                    // Disable the Crashlytics mapping file upload. This feature should only be
                    // enabled if a Firebase backend is available and configured in
                    // google-services.json.
                    configure<CrashlyticsExtension> {
                        mappingFileUploadEnabled = false
                    }
                }
            }
        }
    }
}
