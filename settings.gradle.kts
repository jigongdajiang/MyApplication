pluginManagement {
    // 引入构建管理项目
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "My Application"
include(":app")

// 配置git hooks 规则
val preCommitHook = file(".git/hooks/pre-commit")
val prePushHook = file(".git/hooks/pre-push")
val commitMsgHook = file(".git/hooks/commit-msg")
val hooksInstalled = commitMsgHook.exists()
        && prePushHook.exists()
        && prePushHook.readBytes().contentEquals(file("tools/pre-push").readBytes())
        && preCommitHook.exists()
        && preCommitHook.readBytes().contentEquals(file("tools/pre-commit").readBytes())

if (!hooksInstalled) {
    exec {
        commandLine("tools/setup.sh")
        workingDir = rootProject.projectDir
    }
}