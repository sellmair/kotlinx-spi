package  _KOTLINX_SPI_MODULE

import kotlin.reflect.KClass

fun <T : Any> loadService(service: KClass<T>): Sequence<T> {
    return emptySequence()
}
