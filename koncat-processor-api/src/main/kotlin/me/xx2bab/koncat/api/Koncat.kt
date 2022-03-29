package me.xx2bab.koncat.api

import java.io.File

class Koncat(private val adapter: AnnotationProcessorAdapter) : KoncatAPI {

    init {
        val apiLibVersion = ""
        val pluginVersion = adapter.arguments.koncatVersion
        if (apiLibVersion != pluginVersion) {
            adapter.logger.warn(
                "Koncat Gradle Plugin and Processor API Library " +
                        "use different versions may cause unexpected error."
            )
        }
    }

    override val projectName: String = adapter.arguments.projectName

    override val variantName: String = adapter.variantName

    override fun getGradlePlugins(): List<String> = adapter.arguments.gradlePlugins

    override fun isMainModule(): Boolean = adapter.arguments.declaredAsMainProject

    override fun getIntermediatesDir(): File = File(adapter.arguments.variantAwareIntermediates, variantName)

}