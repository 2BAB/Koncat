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
import me.xx2bab.koncat.api.KoncatProcAPI
import me.xx2bab.koncat.api.KoncatProcMetadata
import me.xx2bab.koncat.contract.KLogger
import me.xx2bab.koncat.processor.anno.AnnotationSubProcessor
import me.xx2bab.koncat.processor.interfaze.ClassTypeSubProcessor
import me.xx2bab.koncat.processor.property.PropertyTypeSubProcessor
import me.xx2bab.koncat.runtime.KoncatExtend
import me.xx2bab.koncat.runtime.KoncatMeta
import java.io.OutputStream
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean


class KoncatAggregationProcessor(
    codeGenerator: CodeGenerator,
    logger: KLogger,
    koncat: KoncatProcAPI
) : KoncatBaseProcessor(codeGenerator, logger, koncat) {

    private val subMetaDataCollected = AtomicBoolean(false)
    private var roundCount = 0
    private var lastMapSize = 0

    override fun process(resolver: Resolver): List<KSAnnotated> {
        // Load meta data from subprojects, put all of them into the `exportMetadata` of main project,
        // therefore the only thing we need to update is `exportMetadata` later in per process round.
        if (!subMetaDataCollected.get()) {
            subMetaDataCollected.set(true)
            logger.info("Query all sub projects meta data from " + koncat.getIntermediatesDir().absolutePath)
            val subProjectMetadataList = extractKoncatMetaList(resolver, exportMetadata)
            subProjectMetadataList.forEach {
                mergeMap(it.annotatedClasses, exportMetadata.annotatedClasses)
                mergeMap(it.typedClasses, exportMetadata.typedClasses)
                mergeMap(it.typedProperties, exportMetadata.typedProperties)
            }
        }

        // Process as usual to collect meta data from new files per round
        val ret = super.process(resolver)

        // For extension file generation, this must be placed in `process()` to be able to notify
        // KSP that the generated file should trigger a new round of process, therefore other processors
        // are aware of the `KoncatExtend` annotated file, and retrieve aggregated metadata from it
        // (as `finish()` does not trigger a new round of process).
        if (koncat.generateExtensionClassEnabled() && exportMetadata.elementSize() > lastMapSize) {
            roundCount++
            lastMapSize = exportMetadata.elementSize()
            val osForKt = codeGenerator.createNewFile(
                dependencies = Dependencies(
                    aggregating = true,
                    *exportMetadata.mapKSFiles.toTypedArray()
                ),
                packageName = metadataPackage,
                fileName = aggregationMetadataFileName + roundCount
            )
            val annotation = KoncatExtend::class.simpleName!!
            osForKt.overwrite(
                """
                package $metadataPackage
                
                import me.xx2bab.koncat.runtime.$annotation
                
                @$annotation(metaDataInJson = ${'"'}""${
                    koncat.getResourceByFileName(aggregationMetadataFileName + roundCount + ".koncat")
                }""${'"'})
                val voidProp$roundCount = null // DO NOT use voidProp directly, the valuable information is placing in `metaDataInJson` above. 
                """.trimIndent()
            )
            osForKt.close()

            val osForRes = codeGenerator.createNewFile(
                dependencies = Dependencies(
                    aggregating = true,
                    *exportMetadata.mapKSFiles.toTypedArray()
                ),
                packageName = "",
                fileName = aggregationMetadataFileName + roundCount,
                extensionName = "koncat"
            )
            osForRes.overwrite(Json.encodeToString(exportMetadata))
            osForRes.close()
        }

        return ret
    }

    @OptIn(KotlinPoetKspPreview::class)
    override fun finish() {
        super.finish()

        // Generate the final file
        if (koncat.generateAggregationClassEnabled()) {
            // To gen the data storage file that will be used by koncat-runtime
            val fb = FileSpec.builder(aggregationPackage, aggregationFinalFileName)
            subProcessors.forEach {
                fb.addProperty(it.onGenerate(exportMetadata, logger))
            }
            fb.build().writeTo(
                codeGenerator,
                Dependencies(true, *exportMetadata.mapKSFiles.toTypedArray())
            )
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
                val meta = it.annotations.first { anno ->
                    anno.shortName.asString() == KoncatMeta::class.simpleName
                }
                Json.decodeFromString<KoncatProcMetadata>(
                    meta.arguments.first().value.toString()
                )
            }
            .toList()
    }

}

open class KoncatMetaDataProcessor(
    codeGenerator: CodeGenerator,
    logger: KLogger,
    koncat: KoncatProcAPI
) : KoncatBaseProcessor(codeGenerator, logger, koncat) {

    override fun finish() {
        super.finish()
        // Generate intermediate kt file
        genMetaFile(
            fileName = metadataFileName.format(koncat.projectName
                .replace("[^A-Za-z0-9 ]".toRegex(), "")
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }),
            packageName = metadataPackage,
            annotation = KoncatMeta::class.simpleName!!,
            codeGenerator = codeGenerator,
            dependencies = exportMetadata.mapKSFiles.toTypedArray()
        )
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
        os.overwrite(
            """
                package $packageName
                
                import me.xx2bab.koncat.runtime.$annotation
                
                @$annotation(metaDataInJson = ${'"'}""${
                Json.encodeToString(exportMetadata).replace("$", "\${'$'}")
            }""${'"'})
                val voidProp = null // DO NOT use voidProp directly, the valuable information is placing in `metaDataInJson` above. 
                """.trimIndent()
        )
        os.close()
    }

}


abstract class KoncatBaseProcessor(
    val codeGenerator: CodeGenerator,
    val logger: KLogger,
    val koncat: KoncatProcAPI
) : SymbolProcessor {

    companion object {
        const val metadataPackage = "me.xx2bab.koncat.runtime.meta"
        const val metadataFileName = "KoncatMetaFor%s"

        const val aggregationPackage = "me.xx2bab.koncat.runtime"
        const val aggregationMetadataFileName = "KoncatAggregatedMeta"
        const val aggregationFinalFileName = "KoncatAggregation"
    }

    protected val exportMetadata = KoncatProcMetadata()

    protected val subProcessors = listOf(
        AnnotationSubProcessor(koncat, exportMetadata, logger),
        ClassTypeSubProcessor(koncat, exportMetadata, logger),
        PropertyTypeSubProcessor(koncat, exportMetadata, logger)
    )


    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.info("[process]")

        // Core procedure for per project
        val ret = subProcessors.flatMap {
            it.onProcess(resolver)
        }

        return ret
    }

    override fun finish() {
        super.finish()
        logger.info("[finish]")
    }

    override fun onError() {
        super.onError()
        logger.info("[onError]")
    }

}

internal fun OutputStream.overwrite(str: String) {
    this.write(str.toByteArray())
}
