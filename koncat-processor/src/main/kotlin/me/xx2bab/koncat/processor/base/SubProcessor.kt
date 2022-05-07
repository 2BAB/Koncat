package me.xx2bab.koncat.processor.base

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.squareup.kotlinpoet.PropertySpec
import me.xx2bab.koncat.api.KoncatProcMetadata
import me.xx2bab.koncat.contract.KLogger

interface SubProcessor {

    fun onProcess(
        resolver: Resolver
    ): List<KSAnnotated>

    fun onGenerate(mergedMetadata: KoncatProcMetadata,
                   logger: KLogger): PropertySpec

}