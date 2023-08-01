package com.gjg.myapplication

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * Kotlin Android 通用配置
 */
internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension<*, *, *, *>,
) {
    commonExtension.apply {
        // 设置编译目标为 Android SDK 版本 33
        compileSdk = 33

        defaultConfig {
            // 设置最低支持的 Android 设备版本为 21
            minSdk = 21
        }

        compileOptions {
            // Up to Java 11 APIs are available through desugaring
            // https://developer.android.com/studio/write/java11-minimal-support-table
            // 将 Java 源代码的兼容性设置为 Java 11 版本
            sourceCompatibility = JavaVersion.VERSION_11
            // 将 Java 目标版本设置为 Java 11 版本
            targetCompatibility = JavaVersion.VERSION_11
            // 启用核心库解析以支持在较旧的 Android 版本上使用 Java 11 API
            isCoreLibraryDesugaringEnabled = true
        }
    }
    // 配置 Kotlin 项目相关选项
    configureKotlin()
    // 获取 'libs' 版本目录扩展
    val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

    dependencies {
        // 添加 'coreLibraryDesugaring' 依赖以支持在较旧的 Android 版本上使用 Java 11 API
        add("coreLibraryDesugaring", libs.findLibrary("android.desugarJdkLibs").get())
    }
}

/**
 * Configure base Kotlin options for JVM (non-Android)
 * 为 JVM（非 Android）配置 Kotlin 基本选项
 */
internal fun Project.configureKotlinJvm() {
    extensions.configure<JavaPluginExtension> {
        // Up to Java 11 APIs are available through desugaring
        // https://developer.android.com/studio/write/java11-minimal-support-table
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    configureKotlin()
}

/**
 * Configure base Kotlin options
 */
private fun Project.configureKotlin() {
    // Use withType to workaround https://youtrack.jetbrains.com/issue/KT-55947
    // 使用 withType 来解决 Kotlin 编译器问题（KT-55947）
    // 分别配置每个 Kotlin 编译任务
    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            // 设置 JVM 目标为 Java 11 版本
            jvmTarget = JavaVersion.VERSION_11.toString()
            // Treat all Kotlin warnings as errors (disabled by default)
            // Override by setting warningsAsErrors=true in your ~/.gradle/gradle.properties
            // 如果在 Gradle 属性中启用了将所有 Kotlin 警告视为错误，则将所有警告视为错误
            val warningsAsErrors: String? by project
            allWarningsAsErrors = warningsAsErrors.toBoolean()
            // 启用某些实验性的 Kotlin 编译器参数
            freeCompilerArgs = freeCompilerArgs + listOf(
                "-opt-in=kotlin.RequiresOptIn",// 启用 '@kotlin.RequiresOptIn' 注解的使用
                // Enable experimental coroutines APIs, including Flow
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",// 启用实验性的协程 API
                "-opt-in=kotlinx.coroutines.FlowPreview",// 启用实验性的 Flow API
            )
        }
    }
}
