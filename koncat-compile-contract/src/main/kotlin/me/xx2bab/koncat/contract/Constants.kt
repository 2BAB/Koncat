package me.xx2bab.koncat.contract

const val KONCAT_FILE_EXTENSION = "koncat"
const val KONCAT_ARGUMENT_INTERMEDIATES_DIR = "KONCAT_VARIANT_AWARE_INTERMEDIATES"
const val KONCAT_ARGUMENT_TARGET_FILE_BASE = "base.json"
const val KONCAT_ARGUMENT_TARGET_FILE_VARIANT_AWARE = "%s.json"

const val LOG_TAG = "[Koncat] "
const val DIRECTORY_PARSE_ERROR = "Koncat directory can not be located from processor arguments."
const val DIRECTORY_NOT_EXIST_ERROR = "Koncat directory can not be found from %s."
const val ARGUMENT_PARSE_ERROR = "Koncat arguments can not match due to %s"
const val AGGREGATION_ERROR = ".koncat file extraction error happened when working with %s, " +
        "this may caused by incompatible Android Gradle Plugin, please try with latest Koncat " +
        "or create an issue on Koncat Github repo https://github.com/2BAB/Koncat"

const val KSP_PLUGIN_NAME = "com.google.devtools.ksp"