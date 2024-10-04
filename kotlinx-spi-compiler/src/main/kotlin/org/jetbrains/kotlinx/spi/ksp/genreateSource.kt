package org.jetbrains.kotlinx.spi.ksp

import org.jetbrains.kotlin.name.FqName

internal fun generateServiceLoaderSourceCode(declarations: List<ServiceDeclaration>): String {
    val bindings = declarations.groupBy { it.service }

    val serviceLoader = """
        package  _KOTLINX_SPI_MODULE

        import kotlin.reflect.KClass
        
        private val services = mapOf<String, List<Any>>(
             ${bindings.entries.joinToString { (service, impls) -> renderBinding(service, impls) }}
        )

        fun <T : Any> loadService(service: KClass<T>): Sequence<T> {
            val qualifiedName = service.qualifiedName ?: return emptySequence()
            return services[qualifiedName]?.asSequence()?.let { it as Sequence<T> } ?: emptySequence()
        }

    """.trimIndent()

    return serviceLoader
}

private fun renderBinding(service: FqName, declarations: List<ServiceDeclaration>): String {
    val implementations = declarations.sortedBy { it.ordinal }.map { it.implementation }
    return """
        "${service.asString()}" to listOf(${implementations.joinToString { it.asString() }}),
    """.trimIndent()
}

