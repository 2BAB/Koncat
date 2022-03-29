package me.xx2bab.koncat.api.tool

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.impl.CodeGeneratorImpl
import java.io.File
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

class VariantAwareness(env: SymbolProcessorEnvironment) {

    val variantName: String

    init {
        val resourcesDir = CodeGeneratorImpl::class.memberProperties
            .first { it.name == "resourcesDir" }
            .also { it.isAccessible = true }
            .getter(env.codeGenerator as CodeGeneratorImpl)
            .toString()
        variantName = File(resourcesDir).parentFile.name
    }

}