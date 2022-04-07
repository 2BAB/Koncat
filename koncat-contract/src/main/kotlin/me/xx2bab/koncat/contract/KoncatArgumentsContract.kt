package me.xx2bab.koncat.contract

import java.io.File

class KoncatArgumentsContract {

    val projectName: String

    val koncatVersion: String

    val gradlePlugins: List<String>

    val declaredAsMainProject: Boolean

    val variantAwareIntermediates: File

    constructor(
        projectName: String,
        koncatVersion: String,
        gradlePlugins: List<String>,
        declaredAsMainProject: Boolean,
        variantAwareIntermediates: File
    ) {
        this.projectName = projectName
        this.gradlePlugins = gradlePlugins
        this.koncatVersion = koncatVersion
        this.declaredAsMainProject = declaredAsMainProject
        this.variantAwareIntermediates = variantAwareIntermediates
    }

    constructor(
        argumentMap: Map<String, String>,
        logger: KLogger
    ) {
        val koncatArgumentMap = argumentMap.filter {
            it.key.startsWith(KONCAT_PROCESSOR_ARGUMENT_KEY)
        }.mapKeys {
            it.key.removePrefix(KONCAT_PROCESSOR_ARGUMENT_KEY)
        }
        check(koncatArgumentMap.size == KoncatArgument.values().size) {
            ARGUMENT_PARSE_ERROR.format("argument map size are different.")
        }
        KoncatArgument.values().forEach { expect ->
            check(koncatArgumentMap.containsKey(expect.name)) {
                ARGUMENT_PARSE_ERROR.format(KONCAT_PROCESSOR_ARGUMENT_KEY + "${expect.name} is not found.")
            }
        }

        for ((k, v) in koncatArgumentMap) {
            logger.info("$LOG_TAG Koncat receives the argument: $k = $v")
        }

        projectName = koncatArgumentMap[KoncatArgument.PROJECT_NAME.name]!!
        koncatVersion = koncatArgumentMap[KoncatArgument.KONCAT_VERSION.name]!!
        gradlePlugins = koncatArgumentMap[KoncatArgument.GRADLE_PLUGINS.name]!!.split(KONCAT_STRING_SEPARATOR)
        declaredAsMainProject = koncatArgumentMap[KoncatArgument.DECLARED_AS_MAIN_PROJECT.name]!!.toBoolean()
        variantAwareIntermediates = File(koncatArgumentMap[KoncatArgument.VARIANT_AWARE_INTERMEDIATES.name]!!)
    }

    fun toMap(): Map<String, String> {
        val map = HashMap<String, String>()
        map[KoncatArgument.PROJECT_NAME.name] = projectName
        map[KoncatArgument.KONCAT_VERSION.name] = koncatVersion
        map[KoncatArgument.GRADLE_PLUGINS.name] = gradlePlugins.joinToString(separator = KONCAT_STRING_SEPARATOR)
        map[KoncatArgument.DECLARED_AS_MAIN_PROJECT.name] = declaredAsMainProject.toString()
        map[KoncatArgument.VARIANT_AWARE_INTERMEDIATES.name] = variantAwareIntermediates.absolutePath
        return map.mapKeys {
            KONCAT_PROCESSOR_ARGUMENT_KEY + it.key
        }
    }

}