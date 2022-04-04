package me.xx2bab.koncat.sample.kotlin

import com.google.devtools.ksp.symbol.KSFile
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class ExportMetadata(
    val exportAPIs: MutableList<String> = mutableListOf(),
    @Transient val mapKSFiles: MutableList<KSFile> = mutableListOf()
)
