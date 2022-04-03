package me.xx2bab.koncat.sample.kotlin

import kotlinx.serialization.Serializable

@Serializable
data class ExportMetadata(
    val exportAPI: MutableList<String> = mutableListOf()
)
