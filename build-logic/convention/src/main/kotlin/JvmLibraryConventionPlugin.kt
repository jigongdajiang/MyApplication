import com.gjg.myapplication.configureKotlinJvm
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * 该插件的作用是应用 Kotlin JVM 插件，并配置相关的 Kotlin JVM 设置，以便在项目中使用 Kotlin 编程语言，并将代码编译为 Java 字节码（JVM 字节码）
 */
class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.jvm")
            }
            configureKotlinJvm()
        }
    }
}
