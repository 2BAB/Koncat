package me.xx2bab.koncat.api

import com.google.devtools.ksp.processing.Resolver
import java.io.File

interface KoncatProcAPI {

    val projectName: String

    val variantName: String

    fun isMainProject(): Boolean

    fun syncAggregatedMetadata(resolver: Resolver): KoncatProcMetadataHolder?

    fun generateExtensionClassEnabled(): Boolean

    fun generateAggregationClassEnabled(): Boolean

    fun getIntermediatesDir(): File

    fun getIntermediatesFiles(): Sequence<File>

    fun getGradlePlugins(): List<String>

    fun getTargetAnnotations(): List<String>

    fun getTargetClassTypes(): List<String>

    fun getTargetPropertyTypes(): List<String>

    fun getResourceByFileName(fileName: String): File
}
