package org.jetbrains.kotlinx.spi.gradle

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.kotlin.dsl.dependencies

private const val SPI_COMPILER_CLASSPATH_CONFIGURATION_NAME = "_spiCompilerClasspath"

internal fun Project.createSpiCompilerClasspathConfiguration(): Configuration {
    return configurations.create(SPI_COMPILER_CLASSPATH_CONFIGURATION_NAME) {
        isCanBeConsumed = false
        isCanBeResolved = true
        project.dependencies {
            this@create("org.jetbrains.kotlinx:kotlinx-spi-compiler:1.0.0-SNAPSHOT")
        }
    }
}

internal fun Project.getSpiCompilerClasspathConfiguration(): Configuration {
    return configurations.getByName(SPI_COMPILER_CLASSPATH_CONFIGURATION_NAME)
}