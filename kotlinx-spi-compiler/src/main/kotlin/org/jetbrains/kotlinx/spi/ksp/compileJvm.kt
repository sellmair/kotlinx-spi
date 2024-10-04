package org.jetbrains.kotlinx.spi.ksp

import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.config.Services
import org.jetbrains.kotlin.incremental.classpathAsList
import org.jetbrains.kotlin.incremental.destinationAsFile
import java.nio.file.Path
import kotlin.io.path.absolute
import kotlin.io.path.absolutePathString

fun compileJvm(
    source: Path,
    classpath: List<Path>,
    output: Path,
) {
    val arguments = K2JVMCompilerArguments()
    arguments.moduleName = "kotlinx.spi.runtime"
    arguments.classpathAsList = classpath.map { it.absolute().toFile() }.toList()
    arguments.destinationAsFile = output.toFile()
    arguments.freeArgs = listOf(source.absolutePathString())
    arguments.friendPaths = classpath.map { it.absolutePathString() }.toTypedArray()
    arguments.noStdlib = true
    K2JVMCompiler().exec(
        PrintingMessageCollector(System.err, MessageRenderer.GRADLE_STYLE, true),
        Services.EMPTY,
        arguments
    )
}