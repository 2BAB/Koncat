package me.xx2bab.koncat.api

import me.xx2bab.koncat.contract.KLogger
import me.xx2bab.koncat.contract.KoncatArgumentsContract
import java.io.File


interface ProcessorAdapter {
    val logger: KLogger
    val intermediateDir: File
    val arguments: KoncatArgumentsContract
    val variantName: String
}

interface KoncatAPI {

    val projectName: String

    val variantName: String

    fun isMainProject(): Boolean

    fun getIntermediatesDir(): File

    fun getGradlePlugins(): List<String>

}
