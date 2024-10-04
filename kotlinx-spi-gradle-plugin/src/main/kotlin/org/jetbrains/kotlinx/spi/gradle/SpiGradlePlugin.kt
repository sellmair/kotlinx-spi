package org.jetbrains.kotlinx.spi.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.exclude
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.kotlin.dsl.withType
import org.gradle.process.ExecOperations
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinMetadataTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import javax.inject.Inject

@Suppress("unused") // Defined by FQN
class SpiGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.createSpiCompilerClasspathConfiguration()

        project.plugins.withType<KotlinBasePlugin>().configureEach {
            project.kotlinExtension.setupSpi()
        }
    }
}

private fun KotlinProjectExtension.setupSpi() {
    this as KotlinMultiplatformExtension
    this.targets.all target@{
        if (this is KotlinMetadataTarget) return@target

        this.compilations.all compilation@{
            val spiCompile = registerSpiCompileTask()

            if (this@target is KotlinJvmTarget) {
                project.configurations.getByName(runtimeDependencyConfigurationName!!)
                    .exclude("org.jetbrains.kotlinx", "kotlinx-spi-runtime")

                if (compilationName == "main") {
                    @Suppress("OPT_IN_USAGE")
                    this@target.mainRun {
                        classpath(project.files({ spiCompile.get().outputFiles }).builtBy(spiCompile))
                    }
                }

                if (compilationName == "test") {
                    this@target.testRuns.getByName("test") testRun@{
                        this@testRun.executionTask.configure {
                            classpath += project.files(spiCompile.map { it.outputFiles })
                        }
                    }
                }
            }

            if (this@target is KotlinNativeTarget) {
                this@target.binaries.all binary@{
                    if (this.compilation != this@compilation) return@binary

                    linkTaskProvider.configure {
                        libraries.setFrom(project.files({
                            project.files(
                                spiCompile.get().outputFiles,
                                this@compilation.compileDependencyFiles
                            )
                        }).builtBy(spiCompile))
                    }
                }

            }

            compileTaskProvider.configure {
                finalizedBy(spiCompile)
            }
        }
    }
}

private fun KotlinCompilation<*>.registerSpiCompileTask(): TaskProvider<SpiCompileTask> {
    return project.tasks.register(
        "compileKotlinSpi${this.target.name.capitalized}${this.name.capitalized}",
        SpiCompileTask::class.java,
        this
    )
}

private open class SpiCompileTask @Inject constructor(
    compilation: KotlinCompilation<*>
) : DefaultTask() {
    private val spiDirectory = project.layout.buildDirectory.dir("spi")

    @get:Internal
    val outputFiles: ConfigurableFileCollection = project.files({
        spiDirectory.map { dir ->
            if (compilation is KotlinNativeCompilation) dir.asFile.resolve("${compilation.target.name}/${compilation.name}/$compilation.klib")
            else dir.asFile.resolve("${compilation.target.name}/${compilation.name}/jar")
        }
    }).builtBy(path)

    init {
        if (compilation is KotlinNativeCompilation) {
            outputs.file(project.provider { outputFiles.singleFile })
        } else {
            outputs.dir(project.provider { outputFiles.singleFile })
        }
    }


    private val exec = project.serviceOf<ExecOperations>()

    @get:Input
    @get:Optional
    val debug: Provider<String> = project.providers.gradleProperty("spi.debug")

    @InputFiles
    val inputFiles: FileCollection =
        project.files({ compilation.runtimeDependencyFiles ?: compilation.compileDependencyFiles })
            .filter { file -> !file.startsWith(spiDirectory.get().asFile) }
            .plus(project.files({ compilation.output.allOutputs }))


    @Classpath
    val compilerClasspath: FileCollection = project.getSpiCompilerClasspathConfiguration()

    @get:Input
    @get:Optional
    protected val konanTarget: String? = (compilation as? KotlinNativeCompilation)?.konanTarget?.name

    @get:Input
    @get:Optional
    protected val konanHome: Provider<String?> = project.provider {
        if (compilation !is KotlinNativeCompilation) return@provider null
        compilation.compileTaskProvider.get().konanHome.get()
    }

    @TaskAction
    fun generate() {
        exec.javaexec {
            this.debug = this@SpiCompileTask.debug.orNull == "true"
            this.classpath = compilerClasspath
            this.mainClass.set("org.jetbrains.kotlinx.spi.ksp.SpiCompiler")
            this.args("--output", outputFiles.singleFile.absolutePath)
            if (konanTarget != null) this.args("--konan-target", konanTarget)
            if (konanHome.isPresent) this.args("--konan-home", konanHome.get())
            this.args(inputFiles.map { it.absolutePath })
        }
    }
}

