package com.gjg.myapplication

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File

/**
 * 配置Android Compose，一下代码对应build的配置为
 * android {
 *   buildFeatures {
 *       compose = true //启动compose
 *   }
 *
 *   composeOptions {
 *       kotlinCompilerExtensionVersion = "1.4.2" //指定Kotlin 编译器扩展版本
 *   }
 *
 *   dependencies {
 *      //引入compose Bom
 *      val composeBom = platform("androidx.compose:compose-bom:2023.01.00")
 *      implementation(composeBom)
 *      androidTestImplementation(composeBom)
 *   }
 * }
 */
internal fun Project.configureAndroidCompose(commonExtension: CommonExtension<*,*,*,*>){
    val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
    commonExtension.apply {
        buildFeatures{
            compose = true
        }

        composeOptions {
            kotlinCompilerExtensionVersion = libs.findVersion("androidxComposeCompiler").get().toString()
        }

        dependencies {
            val bom = libs.findLibrary("androidx-compose-bom").get()
            add("implementation",platform(bom))
            add("androidTestImplementation",platform(bom))
        }
    }
    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            //额外的kotlin编译器参数
            freeCompilerArgs = freeCompilerArgs + buildComposeMetricsParameters()
        }
    }
}

private fun Project.buildComposeMetricsParameters(): List<String> {
    val metricParameters = mutableListOf<String>()
    val enableMetricsProvider = project.providers.gradleProperty("enableComposeCompilerMetrics")
    val enableMetrics = (enableMetricsProvider.orNull == "true")
    //告诉编译器，编译期间度量信息的位置，编译度量信息.
    if(enableMetrics){
        val metricsFolder = File(project.buildDir, "compose-metrics")
        metricParameters.add("-P")
        metricParameters.add(
            "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=" + metricsFolder.absolutePath
        )
    }
    val enableReportsProvider = project.providers.gradleProperty("enableComposeCompilerReports")
    val enableReports = (enableReportsProvider.orNull == "true")
    //告诉编译器，编译后报告的位置
    if (enableReports) {
        val reportsFolder = File(project.buildDir, "compose-reports")
        metricParameters.add("-P")
        metricParameters.add(
            "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=" + reportsFolder.absolutePath
        )
    }
    return metricParameters.toList()
}