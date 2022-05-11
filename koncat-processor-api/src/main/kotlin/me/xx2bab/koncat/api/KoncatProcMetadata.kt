package me.xx2bab.koncat.api

import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFile
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class KoncatProcMetadataHolder(private val koncatExtendAnnotatedElement: KSAnnotated) {

    val dependency: KSFile = koncatExtendAnnotatedElement.containingFile!!

    fun resolve(): KoncatProcMetadata = Json.decodeFromString(
        koncatExtendAnnotatedElement
            .annotations.first()
            .arguments.first()
            .value.toString()
    )

}

/**
 * TODO: break down the [KoncatProcMetadata] to sub processors
 */
@Serializable
data class KoncatProcMetadata(
    val annotatedClasses: MutableAnnotatedClasses = mutableMapOf(),
    val typedClasses: MutableMap<String, MutableList<String>> = mutableMapOf(),
    val typedProperties: MutableMap<String, MutableList<String>> = mutableMapOf(),
    @Transient val mapKSFiles: MutableList<KSFile> = mutableListOf()
) {
    fun elementSize(): Int {
        return (annotatedClasses.map { it.value.size }.sum()
                + typedClasses.map { it.value.size }.sum()
                + typedProperties.map { it.value.size }.sum())
    }
}
typealias MutableAnnotatedClasses = MutableMap<String, MutableList<ClassDeclarationRecord>>

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

