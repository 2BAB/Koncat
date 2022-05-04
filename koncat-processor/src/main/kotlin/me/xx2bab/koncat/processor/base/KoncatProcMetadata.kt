package me.xx2bab.koncat.processor

import com.google.devtools.ksp.symbol.KSFile
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


@Serializable
data class ClassDeclarationRecord(
    val name: String,
    val annotations: List<AnnotationRecord>
)

@Serializable
data class AnnotationRecord(
    val name: String,
    val arguments: Map<String, String>
)

/**
 * TODO: break down the [KoncatProcMetadata] to sub processors
 */
typealias MutableAnnotatedClasses = MutableMap<String, MutableList<ClassDeclarationRecord>>
@Serializable
data class KoncatProcMetadata(
    val annotatedClasses: MutableAnnotatedClasses = mutableMapOf(),
    val interfaces: MutableMap<String, MutableList<String>> = mutableMapOf(),
    val properties: MutableMap<String, MutableList<String>> = mutableMapOf(),
    @Transient val mapKSFiles: MutableList<KSFile> = mutableListOf()
)