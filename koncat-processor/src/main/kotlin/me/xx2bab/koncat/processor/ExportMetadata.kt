package me.xx2bab.koncat.processor

import com.google.devtools.ksp.symbol.KSFile
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class ExportMetadata(
    val markers: MutableList<String> = mutableListOf(),
    @Transient val mapKSFiles: MutableList<KSFile> = mutableListOf()
)
