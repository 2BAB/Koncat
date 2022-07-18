package me.xx2bab.koncat.api

import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.validate
import me._bab.koncat_processor_api.BuildConfig
import me.xx2bab.koncat.api.base.ProcessorAdapter
import me.xx2bab.koncat.contract.KONCAT_FILE_EXTENSION
import me.xx2bab.koncat.runtime.KoncatExtend
import java.io.File

class KoncatProcAPIImpl(private val adapter: ProcessorAdapter) :
    KoncatProcAPI {

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

    override fun syncAggregatedMetadata(resolver: Resolver): KoncatProcMetadataHolder? {
        check(isMainProject()) { "aggregatedMetadata() API can only be called from main project." }
        val koncatExtensions = resolver.getSymbolsWithAnnotation(KoncatExtend::class.qualifiedName!!)
            .filter { it.validate() }
        // The processor may run a few times, so we need to deal with the empty scenario.
        // Only when [KoncatProcessor] from main project has been done,
        // this can run again and return valid [KoncatProcMetadata].
        if (koncatExtensions.count() == 0) {
            return null
        }
        val latestKoncatExtension = koncatExtensions.sortedByDescending { it.containingFile!!.fileName }
            .first()
        return KoncatProcMetadataHolder(latestKoncatExtension)
    }

    override fun generateExtensionClassEnabled(): Boolean = adapter.arguments.generateExtensionClass

    override fun generateAggregationClassEnabled(): Boolean =
        adapter.arguments.generateAggregationClass

    override fun getIntermediatesDir(): File = File(adapter.intermediateDir, variantName)

    override fun getIntermediatesFiles(): Sequence<File> =
        getIntermediatesDir().walk().filter {
            it.extension == KONCAT_FILE_EXTENSION
        }

    override fun getResourceByFileName(fileName: String): File {
        return File(adapter.resourceDir, fileName)
    }

}