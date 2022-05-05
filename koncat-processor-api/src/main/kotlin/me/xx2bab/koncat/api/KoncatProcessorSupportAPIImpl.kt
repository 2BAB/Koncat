package me.xx2bab.koncat.api

import me._bab.koncat_processor_api.BuildConfig
import me.xx2bab.koncat.contract.KONCAT_FILE_EXTENSION
import java.io.File

class KoncatProcessorSupportAPIImpl(private val adapter: ProcessorAdapter) :
    KoncatProcessorSupportAPI {

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

    override fun getTargetAnnotations(): List<String> = adapter.arguments.targetAnnotations

    override fun getTargetClassTypes(): List<String> = adapter.arguments.targetClassTypes

    override fun getTargetPropertyTypes(): List<String> = adapter.arguments.targetPropertyTypes

    override fun isMainProject(): Boolean = adapter.arguments.declaredAsMainProject

    override fun generateExtensionClassEnabled(): Boolean = adapter.arguments.generateExtensionClass

    override fun generateAggregationClassEnabled(): Boolean = adapter.arguments.generateAggregationClass

    override fun getIntermediatesDir(): File = File(adapter.intermediateDir, variantName)

    override fun getIntermediatesFiles(): Sequence<File> =
        getIntermediatesDir().walk().filter {
            it.extension == KONCAT_FILE_EXTENSION
        }


}