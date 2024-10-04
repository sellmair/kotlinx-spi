@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-ide-plugin-dependencies")
        maven("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies")
    }
}

gradle.lifecycle.beforeProject {
    version = "1.0.0-SNAPSHOT"
    group = "org.jetbrains.kotlinx"
}

include(":kotlinx-spi-core")
include(":kotlinx-spi-runtime")
include(":kotlinx-spi-compiler")
include(":kotlinx-spi-gradle-plugin")
