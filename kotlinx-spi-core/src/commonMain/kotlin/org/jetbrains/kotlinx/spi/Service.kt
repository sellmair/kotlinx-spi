package org.jetbrains.kotlinx.spi

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
@Repeatable
annotation class Service(val target: KClass<*>, val ordinal: Int = 0)
