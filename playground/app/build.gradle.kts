import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.kotlinx.spi")
}

kotlin {
    jvm()
    macosArm64()

    macosArm64().binaries.executable {
        entryPoint = "main"
    }

    sourceSets.commonMain.dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-spi-core:1.0.0-SNAPSHOT")
        implementation(project(":core"))
        implementation(project(":libA"))
    }

    sourceSets.commonTest.dependencies {
        implementation(kotlin("test"))
        runtimeOnly(project(":libB"))
    }
}

tasks.register<Exec>("macosRun") {
    dependsOn(kotlin.macosArm64().binaries.getExecutable(NativeBuildType.DEBUG).linkTaskProvider)
    val file = kotlin.macosArm64().binaries.getExecutable(NativeBuildType.DEBUG).outputFile
    inputs.files(file)
    commandLine(file.absolutePath)
}
