package com.gjg.myapplication

import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport
import java.util.Locale

/**
 * Gradle构建工具中的一个插件，用于集成JaCoCo（Java Code Coverage）工具，用于生成Java代码的覆盖率报告。
 * JaCoCo是一个开源的代码覆盖率工具，用于评估在运行测试时代码的覆盖情况，帮助开发者了解哪些代码被测试覆盖，哪些代码没有被覆盖，从而进行测试优化和代码质量改进。
 */

//包含了要在JaCoCo代码覆盖率报告中排除的文件和类
private val coverageExclusions = listOf(
    // Android
    "**/R.class",
    "**/R\$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*"
)

//将字符串的第一个字符转换为大写
private fun String.capitalize() = replaceFirstChar {
    if(it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
}

internal fun Project.configureJacoco(androidComponentsExtension: AndroidComponentsExtension<*,*,*>){
    val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

    //指定JaCoCo的工具版本
    configure<JacocoPluginExtension> {
        toolVersion = libs.findVersion("jacoco").get().toString()
    }

    //创建一个名为jacocoTestReport的Task
    val jacocoTestReport = tasks.create("jacocoTestReport")

    //为每个组件（variant）创建Jacoco报告任务
    androidComponentsExtension.onVariants { variant ->
        //根据variant的名称生成对应的测试任务名称
        val testTaskName = "test${variant.name.capitalize()}UnitTest"

        //创建一个名为jacoco${testTaskName.capitalize()}Report的JacocoReport任务
        val reportTask = tasks.register("jacoco${testTaskName.capitalize()}Report", JacocoReport::class) {
            dependsOn(testTaskName)
            //配置了报告的输出格式（xml和html）
            reports {
                xml.required.set(true)
                html.required.set(true)
            }

            classDirectories.setFrom(
                fileTree("$buildDir/tmp/kotlin-classes/${variant.name}") {
                    exclude(coverageExclusions)
                }
            )
            //代码和源文件的目录
            sourceDirectories.setFrom(files("$projectDir/src/main/java", "$projectDir/src/main/kotlin"))

            //指定了覆盖率数据文件（.exec）的位置
            executionData.setFrom(file("$buildDir/jacoco/$testTaskName.exec"))
        }

        jacocoTestReport.dependsOn(reportTask)
    }

    //全局配置Test Task
    tasks.withType<Test>().configureEach {
        //全局配置JaCoCo插件选项
        configure<JacocoTaskExtension> {
            // Required for JaCoCo + Robolectric
            // https://github.com/robolectric/robolectric/issues/2230
            // TODO: Consider removing if not we don't add Robolectric
            // 处理与Robolectric框架相关的问题
            isIncludeNoLocationClasses = true

            // Required for JDK 11 with the above
            // https://github.com/gradle/gradle/issues/5184#issuecomment-391982009
            excludes = listOf("jdk.internal.*")
        }
    }
}