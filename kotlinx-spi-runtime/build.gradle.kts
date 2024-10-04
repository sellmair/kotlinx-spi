import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")
    `maven-publish`
}

kotlin {
    jvm()
    macosX64()
    macosArm64()

    targets.withType<KotlinNativeTarget>().configureEach {
        compilations.getByName("main").compileTaskProvider.configure {
            compilerOptions.moduleName.set("kotlinx.spi.runtime")
        }
    }
}