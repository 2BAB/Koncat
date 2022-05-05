package me.xx2bab.koncat.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.writeTo
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.xx2bab.koncat.api.KoncatProcessorSupportAPI
import me.xx2bab.koncat.api.KoncatProcessorSupportAPIImpl
import me.xx2bab.koncat.api.adapter.KSPAdapter
import me.xx2bab.koncat.contract.KLogger
import me.xx2bab.koncat.processor.anno.AnnotationSubProcessor
import me.xx2bab.koncat.processor.base.KSPLoggerWrapper
import me.xx2bab.koncat.processor.base.SubProcessor
import me.xx2bab.koncat.processor.interfaze.ClassTypeSubProcessor
import me.xx2bab.koncat.processor.property.PropertyTypeSubProcessor
import me.xx2bab.koncat.runtime.KoncatExtend
import me.xx2bab.koncat.runtime.KoncatMeta
import java.io.OutputStream
import java.util.*

class KoncatProcessorProvider : SymbolProcessorProvider {
    override fun create(
        env: SymbolProcessorEnvironment
    ): SymbolProcessor {
        return KoncatProcessor(
            env.codeGenerator,
            KSPLoggerWrapper(env.logger),
            KoncatProcessorSupportAPIImpl(KSPAdapter(env))
        )
    }
}

class KoncatProcessor(
    private val codeGenerator: CodeGenerator,
    val logger: KLogger,
    private val koncat: KoncatProcessorSupportAPI
) : SymbolProcessor {

    companion object {
        private const val metadataPackage = "me.xx2bab.koncat.runtime.meta"
        private const val metadataFileName = "KoncatMetaFor%s"

        private const val aggregationPackage = "me.xx2bab.koncat.runtime"
        private const val aggregationMetadataFileName = "KoncatAggregatedMeta"
        private const val aggregationFinalFileName = "KoncatAggregation"
    }

    private val subProcessors = listOf(
        AnnotationSubProcessor(),
        ClassTypeSubProcessor(),
        PropertyTypeSubProcessor()
    )
    private val exportMetadata = KoncatProcMetadata()
    private var subProjectMetadataList: List<KoncatProcMetadata>? = null


    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.info("[process]")

        // Core procedure for per project
        subProcessors.forEach {
            it.onProcess(
                resolver,
                koncat,
                exportMetadata,
                logger
            )
        }

        // Load meta data from subprojects (for main Project only)
        if (koncat.isMainProject()) {
            subProjectMetadataList = extractKoncatMetaList(resolver, exportMetadata)
        }

        return emptyList()
    }

    @OptIn(KotlinPoetKspPreview::class)
    override fun finish() {
        super.finish()
        logger.info("[finish]")
        if (koncat.isMainProject()) {
            logger.info(
                "Query all sub projects meta data from "
                        + koncat.getIntermediatesDir().absolutePath
            )
            // Merge all ExportMetadata
            val all = mutableListOf<KoncatProcMetadata>()
            all.add(exportMetadata)
            all.addAll(subProjectMetadataList ?: listOf())
            // Generate the final file
            RouterClassBuilder(
                all,
                subProcessors,
                koncat,
                codeGenerator,
                *exportMetadata.mapKSFiles.toTypedArray()
            ).build()
        } else {
            // Generate intermediate kt file
            genMetaFile(
                fileName = metadataFileName.format(koncat.projectName
                    .replace("[^A-Za-z0-9 ]".toRegex(), "")
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }),
                packageName = metadataPackage,
                annotation = KoncatMeta::class.simpleName!!,
                codeGenerator = codeGenerator,
                dependencies = *exportMetadata.mapKSFiles.toTypedArray()
            )
        }
    }

    override fun onError() {
        super.onError()
        logger.info("[onError]")
    }

    @OptIn(KspExperimental::class)
    private fun extractKoncatMetaList(
        resolver: Resolver,
        mainMetadata: KoncatProcMetadata,
    ): List<KoncatProcMetadata> {
        return resolver.getDeclarationsFromPackage(metadataPackage)
            .filter {
                it is KSPropertyDeclaration
                        && it.annotations.any { anno -> anno.shortName.asString() == KoncatMeta::class.simpleName }
            }
            .map {
                logger.info("Aggregate from ${it.qualifiedName?.asString()}")
                it.containingFile?.let { mainMetadata.mapKSFiles.add(it) }
                val meta =
                    it.annotations.first { anno -> anno.shortName.asString() == KoncatMeta::class.simpleName }
                Json.decodeFromString<KoncatProcMetadata>(
                    meta.arguments.first().value.toString()
                )
            }.toList()
    }

    private fun genMetaFile(
        fileName: String,
        packageName: String,
        annotation: String,
        dependencies: Array<KSFile>,
        codeGenerator: CodeGenerator
    ) {
        val os = codeGenerator.createNewFile(
            Dependencies(aggregating = true, *dependencies),
            packageName,
            fileName,
            "kt"
        )
        os.appendText(
            """
                package $metadataPackage
                
                import me.xx2bab.koncat.runtime.$annotation
                
                @$annotation(metaDataInJson = ${'"'}""${
                Json.encodeToString(exportMetadata).replace("$", "\${'$'}")
            }""${'"'})
                val voidProp = null // DO NOT use voidProp directly, the valuable information is placing in `metaDataInJson` above. 
                """.trimIndent()
        )
        os.close()
    }


    inner class RouterClassBuilder(
        private val dataList: List<KoncatProcMetadata>,
        private val subProcessors: List<SubProcessor>,
        private val koncat: KoncatProcessorSupportAPI,
        private val codeGenerator: CodeGenerator,
        private val dependencies: Array<KSFile>
    ) {
        @OptIn(KotlinPoetKspPreview::class)
        fun build() {
            val aggregatedMeta = KoncatProcMetadata()
            dataList.forEach {
                mergeMap(it.annotatedClasses, aggregatedMeta.annotatedClasses)
                mergeMap(it.typedClasses, aggregatedMeta.typedClasses)
                mergeMap(it.typedProperties, aggregatedMeta.typedProperties)
            }
            if (koncat.generateExtensionClassEnabled()) {
                genMetaFile(
                    fileName = aggregationMetadataFileName,
                    packageName = aggregationPackage,
                    annotation = KoncatExtend::class.simpleName!!,
                    codeGenerator = codeGenerator,
                    dependencies = dependencies
                )
            }
            if (koncat.generateAggregationClassEnabled()) {
                // To gen the data storage file that will be used by koncat-runtime
                val fb = FileSpec.builder(aggregationPackage, aggregationFinalFileName)
                subProcessors.forEach {
                    fb.addProperty(it.onGenerate(aggregatedMeta, logger))
                }
                fb.build().writeTo(codeGenerator, Dependencies(true, *dependencies))
            }
        }

        private fun <R> mergeMap(
            from: MutableMap<String, MutableList<R>>,
            to: MutableMap<String, MutableList<R>>
        ) {
            from.forEach { (key, value) ->
                if (to.containsKey(key)) {
                    to[key]?.addAll(value)
                } else {
                    to[key] = value
                }
            }
        }
    }

}

internal fun OutputStream.appendText(str: String) {
    this.write(str.toByteArray())
}
