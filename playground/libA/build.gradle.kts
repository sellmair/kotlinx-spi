plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm()
    macosX64()
    macosArm64()

    sourceSets.commonMain.dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-spi-core:1.0.0-SNAPSHOT")
        implementation(project(":core"))
    }
}