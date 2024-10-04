package org.jetbrains.kotlinx.spi

import kotlin.reflect.KClass


inline fun <reified T : Any> loadService(): Sequence<T> {
    return loadService(T::class)
}

@PublishedApi
internal fun <T : Any> loadService(service: KClass<T>): Sequence<T> {
    return _KOTLINX_SPI_MODULE.loadService(service)
}