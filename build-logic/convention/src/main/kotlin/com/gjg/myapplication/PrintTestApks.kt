package com.gjg.myapplication

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.BuiltArtifactsLoader
import com.android.build.api.variant.HasAndroidTest
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * 这里定义了一个打印 APK 位置的任务，用于在 AndroidTest 中打印测试 APK 文件的位置
 */
internal fun Project.configurePrintApksTask(extension: AndroidComponentsExtension<*, *, *>) {
    // 通过 AndroidComponentsExtension 的 onVariants 方法，针对每个 variant 进行配置
    extension.onVariants { variant ->
        if (variant is HasAndroidTest) {
            // 获取用于加载构建产物的 BuiltArtifactsLoader
            val loader = variant.artifacts.getBuiltArtifactsLoader()
            // 获取 AndroidTest 的 APK 构建产物
            val artifact = variant.androidTest?.artifacts?.get(SingleArtifact.APK)
            // 获取 AndroidTest 的 Java 源码目录和 Kotlin 源码目录
            val javaSources = variant.androidTest?.sources?.java?.all
            val kotlinSources = variant.androidTest?.sources?.kotlin?.all
            // 将 Java 源码目录和 Kotlin 源码目录合并成一个 List，用于传递给任务
            val testSources = if (javaSources != null && kotlinSources != null) {
                javaSources.zip(kotlinSources) { javaDirs, kotlinDirs ->
                    javaDirs + kotlinDirs
                }
            } else javaSources ?: kotlinSources
            // 如果存在 APK 构建产物和源码目录，则注册一个自定义任务 PrintApkLocationTask
            if (artifact != null && testSources != null) {
                tasks.register(
                    "${variant.name}PrintTestApk",
                    PrintApkLocationTask::class.java
                ) {
                    // 设置任务的输入属性
                    apkFolder.set(artifact)
                    builtArtifactsLoader.set(loader)
                    variantName.set(variant.name)
                    sources.set(testSources)
                }
            }
        }
    }
}

internal abstract class PrintApkLocationTask : DefaultTask() {
    //在属性的getter方法上加上InputDirectory注解，标记 Gradle 任务输入属性
    @get:InputDirectory
    abstract val apkFolder: DirectoryProperty

    @get:InputFiles
    abstract val sources: ListProperty<Directory>

    @get:Internal
    abstract val builtArtifactsLoader: Property<BuiltArtifactsLoader>

    @get:Input
    abstract val variantName: Property<String>

    @TaskAction
    fun taskAction() {
        // 检查 AndroidTest 的源码目录是否存在非生成目录的文件，用于判断是否需要打印 APK 位置
        val hasFiles = sources.orNull?.any { directory ->
            directory.asFileTree.files.any {
                it.isFile && it.parentFile.path.contains("build${File.separator}generated").not()
            }
        } ?: throw RuntimeException("Cannot check androidTest sources")

        // Don't print APK location if there are no androidTest source files
        if (!hasFiles) {
            return
        }
        // 加载 APK 构建产物，并打印 APK 文件的位置
        val builtArtifacts = builtArtifactsLoader.get().load(apkFolder.get())
            ?: throw RuntimeException("Cannot load APKs")
        if (builtArtifacts.elements.size != 1)
            throw RuntimeException("Expected one APK !")
        val apk = File(builtArtifacts.elements.single().outputFile).toPath()
        println(apk)
    }
}