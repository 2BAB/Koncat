package me.xx2bab.koncat.api

import me._bab.koncat_processor_api.BuildConfig
import java.io.File

class Koncat(private val adapter: ProcessorAdapter) : KoncatAPI {

    init {
        val apiLibVersion = BuildConfig.KONCAT_VERSION
        val pluginVersion = adapter.arguments.koncatVersion
        if (apiLibVersion != pluginVersion) {
            adapter.logger.warn(
                "Koncat Gradle Plugin(${pluginVersion}) and Processor API Library(${apiLibVersion}) " +
                        "use different versions may cause unexpected error."
            )
        }
    }

    override val projectName: String = adapter.arguments.projectName

    override val variantName: String = adapter.variantName

    override fun getGradlePlugins(): List<String> = adapter.arguments.gradlePlugins

    override fun isMainProject(): Boolean = adapter.arguments.declaredAsMainProject

    override fun getIntermediatesDir(): File = File(adapter.intermediateDir, variantName)

}