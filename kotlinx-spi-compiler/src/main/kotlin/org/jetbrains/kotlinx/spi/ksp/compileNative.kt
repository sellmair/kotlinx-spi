package org.jetbrains.kotlinx.spi.ksp

import org.jetbrains.kotlin.cli.common.arguments.K2NativeCompilerArguments
import org.jetbrains.kotlin.compilerRunner.toArgumentStrings
import org.jetbrains.kotlin.konan.target.CompilerOutputKind
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File
import java.net.URLClassLoader
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.name

fun compileNative(
    source: Path,
    classpath: List<Path>,
    output: Path,
    konanHome: Path,
    konanTarget: KonanTarget
) {
    val args = K2NativeCompilerArguments()
    val filteredClasspath = classpath.filter { it.exists() }

    args.moduleName = "kotlinx.spi.runtime"
    args.nostdlib = true
    args.libraries = filteredClasspath.map { it.absolutePathString() }.toTypedArray()
    args.friendModules = filteredClasspath.joinToString(File.pathSeparator) { it.absolutePathString() }
    args.freeArgs = listOf(source.absolutePathString())
    args.target = konanTarget.name
    args.produce = CompilerOutputKind.LIBRARY.name
    args.outputName = output.absolutePathString()
    args.includeBinaries

    val konanJar = konanHome.resolve("konan/lib/kotlin-native-compiler-embeddable.jar")
    val trove4jJar = konanHome.resolve("konan/lib/trove4j.jar")
    val konanClassLoader = URLClassLoader(
        arrayOf(konanJar.toUri().toURL(), trove4jJar.toUri().toURL()),
        ClassLoader.getPlatformClassLoader()
    )

    val main = konanClassLoader.loadClass("org.jetbrains.kotlin.cli.utilities.MainKt")
    val arguments = (listOf("konanc") + args.toArgumentStrings())

    main.getDeclaredMethod("main", Array<String>::class.java).invoke(null, arguments.toTypedArray())
}