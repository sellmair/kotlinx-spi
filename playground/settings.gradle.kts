pluginManagement {
    includeBuild("..")

    repositories {
        gradlePluginPortal()
        mavenCentral()

        mavenLocal {
            mavenContent {
                includeGroupByRegex("org.jetbrains.kotlinx.*")
            }
        }
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        mavenLocal {
            mavenContent {
                includeGroup("org.jetbrains.kotlinx")
            }
        }
    }
}

includeBuild("..")

include(":core")
include(":libA")
include(":libB")
include(":app")