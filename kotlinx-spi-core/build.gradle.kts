import org.gradle.internal.impldep.org.apache.tools.zip.ZipFile
import org.gradle.kotlin.dsl.support.unzipTo
import org.gradle.kotlin.dsl.support.zipTo
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeCompilation
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

plugins {
    kotlin("multiplatform")
    `maven-publish`
}

kotlin {
    jvm()
    macosX64()
    macosArm64()

    sourceSets.commonMain.dependencies {
        implementation(project(":kotlinx-spi-runtime"))
    }

    tasks.withType<KotlinNativeCompile>().configureEach {
        /* Patch the manifest */
        if (properties.contains("patch.manifest"))
            doLast {
                val klib = outputFile.get()
                val unzipped = klib.parentFile.resolve("unzipped")
                unzipTo(unzipped, klib)
                if (!unzipped.resolve("default/targets").exists()) {
                    error("DOES NOT EXIST?")
                }
                klib.delete()

                val manifest = unzipped.resolve("default/manifest")
                manifest.writeText(
                    manifest.readText()
                        .replace("depends=stdlib org.jetbrains.kotlinx\\:kotlinx-spi-compiletime", "depends=stdlib")
                )

                ZipOutputStream(klib.outputStream()).use { out ->
                    unzipped.walkTopDown().forEach { file ->
                        if (file.isDirectory) {
                            out.putNextEntry(ZipEntry(file.relativeTo(unzipped).path + "/"))
                            out.closeEntry()
                        }

                        if (file.isFile) {
                            out.putNextEntry(ZipEntry(file.relativeTo(unzipped).path))
                            file.inputStream().use { input -> input.copyTo(out) }
                            out.closeEntry()
                        }
                    }
                }
            }
    }
}

