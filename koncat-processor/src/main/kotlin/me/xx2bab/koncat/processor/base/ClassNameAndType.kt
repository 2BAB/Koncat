package me.xx2bab.koncat.processor.base

import com.google.devtools.ksp.symbol.KSType

data class ClassNameAndType(
    val canonicalName: String,
    val type: KSType
)