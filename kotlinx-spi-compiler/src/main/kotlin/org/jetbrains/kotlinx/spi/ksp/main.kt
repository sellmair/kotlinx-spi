@file:JvmName("SpiCompiler")
@file:OptIn(UnstableMetadataApi::class)

package org.jetbrains.kotlinx.spi.ksp


import kotlinx.metadata.jvm.UnstableMetadataApi
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.library.ToolingSingleFileKlibResolveStrategy
import org.jetbrains.kotlin.library.resolveSingleFileKlib
import java.nio.file.Path
import kotlin.io.path.*
import org.jetbrains.kotlin.konan.file.File as KonanFile

data class Arguments(
    val output: Path,
    val classpath: List<Path>,
    val konanTarget: KonanTarget?,
    val konanHome: Path?,
)


fun main(args: Array<String>) {
    val arguments = args.toList().listIterator()

    var output = "build/spi"
    val classpath = mutableListOf<String>()
    var konanTarget: String? = null
    var konanHome: String? = null

    while (arguments.hasNext()) {
        when (val arg = arguments.next()) {
            "--output" -> output = arguments.next()
            "--konan-target" -> konanTarget = arguments.next()
            "--konan-home" -> konanHome = arguments.next()
            else -> classpath += arg
        }
    }

    val parsed = Arguments(
        output = Path(output),
        classpath = classpath.map { Path(it) },
        konanTarget = konanTarget?.let { KonanTarget.predefinedTargets[it]!! },
        konanHome = konanHome?.let { Path(it) },
    )

    compile(parsed)
}

fun compile(arguments: Arguments) {
    val services: List<ServiceDeclaration> = arguments.classpath.flatMap { binary ->
        if (!binary.exists()) return@flatMap emptyList<ServiceDeclaration>()

        if (binary.extension == "klib") {
            val resolvedKlib = resolveSingleFileKlib(KonanFile(binary), strategy = ToolingSingleFileKlibResolveStrategy)
            return@flatMap readLibrary(resolvedKlib)
        }

        if (binary.extension == "jar") {
            return@flatMap readJar(binary)
        }

        if (binary.isDirectory()) {
            return@flatMap readDirectory(binary)
        }

        emptyList()
    }
    println("Found services: $services")


    val serviceLoaderCode = generateServiceLoaderSourceCode(services)
    val serviceLoaderFile = arguments.output.resolveSibling("source/ServiceLoader.kt")
    serviceLoaderFile.createParentDirectories()
    serviceLoaderFile.writeText(serviceLoaderCode)

    if (arguments.konanTarget != null) {
        compileNative(
            serviceLoaderFile,
            arguments.classpath,
            arguments.output,
            arguments.konanHome!!,
            arguments.konanTarget
        )
    } else {
        compileJvm(serviceLoaderFile, arguments.classpath, arguments.output)
    }
}




