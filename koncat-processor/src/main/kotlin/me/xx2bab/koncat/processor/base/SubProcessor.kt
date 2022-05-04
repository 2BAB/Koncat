package me.xx2bab.koncat.processor.base

import com.google.devtools.ksp.processing.Resolver
import com.squareup.kotlinpoet.PropertySpec
import me.xx2bab.koncat.api.KoncatProcessorSupportAPI
import me.xx2bab.koncat.contract.KLogger
import me.xx2bab.koncat.processor.KoncatProcMetadata

interface SubProcessor {

    fun onProcess(
        resolver: Resolver,
        koncat: KoncatProcessorSupportAPI,
        exportMetadata: KoncatProcMetadata,
        logger: KLogger
    )

    fun onGenerate(mergedMetadata: KoncatProcMetadata,
                   logger: KLogger): PropertySpec

}