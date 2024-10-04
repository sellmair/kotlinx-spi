package org.jetbrains.kotlinx.spi.ksp

import org.jetbrains.kotlin.library.KotlinLibrary
import org.jetbrains.kotlin.library.metadata.KlibMetadataProtoBuf
import org.jetbrains.kotlin.library.metadata.parseModuleHeader
import org.jetbrains.kotlin.library.metadata.parsePackageFragment
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.metadata.deserialization.NameResolverImpl
import org.jetbrains.kotlin.metadata.deserialization.getExtensionOrNull
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import java.nio.file.Path
import kotlin.io.path.Path


internal fun readLibrary(library: KotlinLibrary): Set<ServiceDeclaration> {
    val headerProto = parseModuleHeader(library.moduleHeaderData)

    val packageMetadataSequence = headerProto.packageFragmentNameList.asSequence().flatMap { packageFragmentName ->
        library.packageMetadataParts(packageFragmentName).asSequence().map { packageMetadataPart ->
            library.packageMetadata(packageFragmentName, packageMetadataPart)
        }
    }

    return packageMetadataSequence.flatMap { packageMetadata ->
        val packageFragmentProto = parsePackageFragment(packageMetadata)
        val context = PackageFragmentReadingContext(library, packageFragmentProto)
        context.readServices(packageFragmentProto)
    }.toSet()
}

@Suppress("CONTEXT_RECEIVERS_DEPRECATED")
internal fun PackageFragmentReadingContext.readServices(fragment: ProtoBuf.PackageFragment): Set<ServiceDeclaration> {
    return fragment.class_List.flatMap { classProto ->
        val classId = ClassId.fromString(nameResolver.getQualifiedClassName(classProto.fqName))
        if (classId.isNestedClass) return@flatMap emptyList<ServiceDeclaration>()
        classProto.getExtension(KlibMetadataProtoBuf.classAnnotation).mapNotNull { annotation ->
            val name = nameResolver.getQualifiedClassName(annotation.id)
            if (name != SPI_SERVICE_ANNOTATION_FQN) return@mapNotNull null
            val service = nameResolver.getQualifiedClassName(annotation.argumentList[0].value.classId)
            val ordinal = annotation.argumentList.getOrNull(1)?.value?.intValue?.toInt()
            ServiceDeclaration(
                ClassId.fromString(service).asSingleFqName(),
                classId.asSingleFqName(), ordinal ?: 0
            )
        }
    }.toSet()
}

internal fun PackageFragmentReadingContext(
    library: KotlinLibrary,
    packageFragmentProto: ProtoBuf.PackageFragment,
): PackageFragmentReadingContext {
    val nameResolver = NameResolverImpl(packageFragmentProto.strings, packageFragmentProto.qualifiedNames)
    val packageFqName = packageFragmentProto.`package`.getExtensionOrNull(KlibMetadataProtoBuf.packageFqName)
        ?.let { packageFqNameStringIndex -> nameResolver.getPackageFqName(packageFqNameStringIndex) } ?: ""
    return PackageFragmentReadingContext(Path(library.libraryFile.path), FqName(packageFqName), nameResolver)
}

internal class PackageFragmentReadingContext(
    val libraryPath: Path,
    val packageFqName: FqName,
    val nameResolver: NameResolverImpl,
)