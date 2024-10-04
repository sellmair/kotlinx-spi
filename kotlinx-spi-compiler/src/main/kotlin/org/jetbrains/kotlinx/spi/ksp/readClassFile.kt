package org.jetbrains.kotlinx.spi.ksp

import org.jetbrains.kotlin.name.FqName
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import kotlin.io.path.*
import kotlin.streams.asSequence


internal fun readJar(file: Path): Set<ServiceDeclaration> {
    return ZipFile(file.toFile()).use { zip ->
        zip.stream().asSequence()
            .filter { it.name.endsWith(".class") }
            .flatMap { zip.readClassFile(it) }
            .toSet()
    }
}


internal fun ZipFile.readClassFile(entry: ZipEntry): Sequence<ServiceDeclaration> {
    return readClassFile(getInputStream(entry).use { input -> input.readBytes() })
}

@OptIn(ExperimentalPathApi::class)
internal fun readDirectory(directory: Path): Set<ServiceDeclaration> {
    return directory.walk()
        .filter { it.name.endsWith(".class") && it.isRegularFile() }
        .flatMap { current ->
            readClassFile(current.readBytes())
        }
        .toSet()
}

internal fun readClassFile(clazz: ByteArray): Sequence<ServiceDeclaration> {
    val reader = ClassReader(clazz)
    val visitor = ClassVisitorImpl()
    reader.accept(visitor, /* parsingOptions = */ 0)
    return visitor.declarations.asSequence()
}

private class ClassVisitorImpl : ClassVisitor(Opcodes.ASM9) {
    private var className: String? = null
    val declarations = mutableListOf<ServiceDeclaration>()

    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        className = name
    }

    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? {
        if (descriptor == "L$SPI_SERVICE_ANNOTATION_FQN;") {
            return AnnotationVisitorImpl { ordinal, service ->
                declarations.add(ServiceDeclaration(FqName(service), FqName(className!!), ordinal))
            }
        }
        return null
    }
}

private class AnnotationVisitorImpl(
    val finished: (ordinal: Int, service: String) -> Unit
) : AnnotationVisitor(Opcodes.ASM9) {

    var ordinal = 0

    var service: String? = null

    override fun visit(name: String?, value: Any?) {
        if (name == "ordinal" && value is Int) {
            ordinal = value
        }

        if (name == "target") {
            service = (value as Type).className
        }
    }

    override fun visitEnd() {
        finished(ordinal, service!!)
    }
}