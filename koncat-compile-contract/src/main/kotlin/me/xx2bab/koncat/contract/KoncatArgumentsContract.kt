package me.xx2bab.koncat.contract

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File

@Serializable
data class KoncatArgumentsContract(
    val projectName: String,
    val koncatVersion: String,
    val gradlePlugins: List<String>,
    val declaredAsMainProject: Boolean,
    val extendable: Boolean,
    val targetAnnotations: List<String>,
    val targetInterfaces: List<String>,
    val targetProperties: List<String>
)

fun parseKoncatArguments(targetDirectory: File, variantName: String = ""): KoncatArgumentsContract {
    val fileName = if (variantName.isBlank()) {
        KONCAT_ARGUMENT_TARGET_FILE_BASE
    } else {
        KONCAT_ARGUMENT_TARGET_FILE_VARIANT_AWARE.format(variantName)
    }
    val jsonFile = File(targetDirectory, fileName)
    return Json.decodeFromStream(jsonFile.inputStream())
}