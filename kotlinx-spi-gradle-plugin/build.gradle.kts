plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    `maven-publish`
}

dependencies {
    compileOnly(kotlin("gradle-plugin:2.0.20"))
}

gradlePlugin {
    plugins {
        create("org.jetbrains.kotlinx.spi") {
            id = "org.jetbrains.kotlinx.spi"
            implementationClass = "org.jetbrains.kotlinx.spi.gradle.SpiGradlePlugin"
        }
    }
}