import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.gradle.LibraryExtension
import com.gjg.myapplication.configureFlavors
import com.gjg.myapplication.configureGradleManagedDevices
import com.gjg.myapplication.configureKotlinAndroid
import com.gjg.myapplication.configurePrintApksTask
import com.gjg.myapplication.disableUnnecessaryAndroidTests
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.kotlin

/**
 * 应用于 Android Library 项目，用于配置和定制项目的构建过程
 */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    // 在插件应用时会调用该方法
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                // 应用 com.android.library 插件
                apply("com.android.library")
                // 应用 org.jetbrains.kotlin.android 插件
                apply("org.jetbrains.kotlin.android")
            }

            // 配置 LibraryExtension 扩展
            extensions.configure<LibraryExtension> {
                // 调用 configureKotlinAndroid 函数，可能用于配置 Kotlin 与 Android 集成
                configureKotlinAndroid(this)
                // 设置默认配置中的 targetSdkVersion 为 33
                defaultConfig.targetSdk = 33
                // 调用 configureFlavors 函数，配置产品风味（Flavors）
                configureFlavors(this)
                // 调用 configureGradleManagedDevices 函数，配置 Gradle 管理的设备
                configureGradleManagedDevices(this)
            }

            // 配置 LibraryAndroidComponentsExtension 扩展
            extensions.configure<LibraryAndroidComponentsExtension> {
                // 调用 configurePrintApksTask 函数，配置打印 APK 文件的任务
                configurePrintApksTask(this)
                // 调用 disableUnnecessaryAndroidTests 函数，可能禁用一些不必要的 Android 测试
                disableUnnecessaryAndroidTests(target)
            }

            // 获取名为 "libs" 的版本目录
            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
            // 配置每个配置（Configuration）
            configurations.configureEach {
                // 设置依赖解析策略，强制使用指定的依赖项版本
                resolutionStrategy {
                    // 强制使用 libs 中的 "junit4" 依赖项版本
                    force(libs.findLibrary("junit4").get())
                    // 强制使用指定版本的 "org.objenesis:objenesis"
                    // 这是一个临时的问题解决方案
                    force("org.objenesis:objenesis:2.6")
                }
            }

            // 定义项目的依赖项配置
            dependencies {
                // 在 androidTest 配置中添加 Kotlin 的测试依赖项
                add("androidTestImplementation", kotlin("test"))
                // 在 test 配置中添加 Kotlin 的测试依赖项
                add("testImplementation", kotlin("test"))
            }
        }
    }
}