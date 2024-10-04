package org.jetbrains.kotlinx.spi.ksp

import org.jetbrains.kotlin.name.FqName

data class ServiceDeclaration(val service: FqName, val implementation: FqName, val ordinal: Int)