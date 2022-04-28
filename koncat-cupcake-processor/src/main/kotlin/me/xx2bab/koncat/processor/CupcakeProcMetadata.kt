package me.xx2bab.koncat.processor

import com.google.devtools.ksp.symbol.KSFile
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class CupcakeProcMetadata(
    val annotatedClasses: MutableMap<String, MutableList<ClassDeclarationRecord>> = mutableMapOf(),
    @Transient val mapKSFiles: MutableList<KSFile> = mutableListOf()
)

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