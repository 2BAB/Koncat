package me.xx2bab.koncat.cupcake

data class ClassDeclarationRecord(
    val name: String,
    val annotations: List<AnnotationRecord>
)

data class AnnotationRecord(
    val name: String,
    val arguments: Map<String, String>
)
