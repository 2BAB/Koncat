package me.xx2bab.koncat.contract

// Options
const val KONCAT_FILE_EXTENSION = "koncat"
const val KONCAT_PROCESSOR_ARGUMENT_KEY = "KONCAT_"
const val KONCAT_STRING_SEPARATOR = "<@KONCAT@>"

// Logs
const val LOG_TAG = "[Koncat] "
const val ARGUMENT_PARSE_ERROR = "Koncat arguments can not match due to %s"
const val AGGREGATION_ERROR = ".koncat file extraction error happened when working with %s, " +
        "this may caused by incompatible Android Gradle Plugin, please try with latest Koncat " +
        "or create an issue on Koncat Github repo https://github.com/2BAB/Koncat"

// KCP
const val DEFAULT_COMPILER_PLUGIN_ID = "koncat-compiler-plugin"
