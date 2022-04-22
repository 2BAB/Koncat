package me.xx2bab.koncat.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.writeTo
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import me.xx2bab.koncat.api.Koncat
import me.xx2bab.koncat.api.adapter.KSPAdapter
import me.xx2bab.koncat.contract.KONCAT_FILE_EXTENSION
import me.xx2bab.koncat.contract.LOG_TAG
import java.io.OutputStream

class KoncatProcessorProvider : SymbolProcessorProvider {
    override fun create(
        env: SymbolProcessorEnvironment
    ): SymbolProcessor {
        return  KoncatProcessor(
            env.codeGenerator,
            env.logger,
            Koncat(KSPAdapter(env))
        )
    }
}

class  KoncatProcessor(
    val codeGenerator: CodeGenerator,
    val logger: KSPLogger,
    val koncat: Koncat
) : SymbolProcessor {

    companion object {
        private const val id = "-koncat-proc$"
    }
    private var exportMetadata = ExportMetadata()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.info("$LOG_TAG [process]")

        val symbols =
            resolver.getSymbolsWithAnnotation("me.xx2bab.koncat.sample.annotation.ClassMark")
        val ret = symbols.filter { !it.validate() }.toList()
        symbols
            .filter { (it is KSClassDeclaration || it is KSPropertyDeclaration) && it.validate() }
            .forEach { it.accept(BuilderVisitor(), exportMetadata) }
        return ret
    }

    @OptIn(KotlinPoetKspPreview::class)
    override fun finish() {
        super.finish()
        logger.info("$LOG_TAG [finish]")
        if (koncat.isMainProject()) {
            logger.info(
                LOG_TAG + "Query all sub projects meta data from "
                        + koncat.getIntermediatesDir().absolutePath
            )
            // Merge all ExportMetadata
            val subProjectMetadataList = koncat.getIntermediatesFiles()
                .filter { it.name.contains(id) }
                .map { subProjectMetadataFile ->
                    logger.info(LOG_TAG + "Start processing ${subProjectMetadataFile.absolutePath}")
                    Json.decodeFromStream<ExportMetadata>(
                        subProjectMetadataFile.inputStream()
                    )
                }
            val all = mutableListOf<ExportMetadata>()
            all.add(exportMetadata)
            all.addAll(subProjectMetadataList)
            // Generate the final file
            val fileSpec = RouterClassBuilder(all).build()
            fileSpec.writeTo(codeGenerator, Dependencies.ALL_FILES)
        } else {
            // Generate intermediate JSON file
            val os = codeGenerator.createNewFile(
                Dependencies(aggregating = true, *exportMetadata.mapKSFiles.toTypedArray()),
                "",
                koncat.projectName + id,
                "json.$KONCAT_FILE_EXTENSION"
            )
            os.appendText(Json.encodeToString(exportMetadata))
            os.close()
        }
    }

    override fun onError() {
        super.onError()
        logger.info("$LOG_TAG [onError]")
    }

    inner class BuilderVisitor : KSVisitorWithExportMetadata() {
        override fun visitClassDeclaration(
            classDeclaration: KSClassDeclaration,
            data: ExportMetadata
        ) {
            data.markers.add(classDeclaration.qualifiedName!!.asString())
            classDeclaration.containingFile?.let { data.mapKSFiles.add(it) }
        }

        override fun visitFile(file: KSFile, data: ExportMetadata) {
            super.visitFile(file, data)
        }

        override fun visitPropertyDeclaration(
            property: KSPropertyDeclaration,
            data: ExportMetadata
        ) {
            super.visitPropertyDeclaration(property, data)
            
        }
    }

    inner class RouterClassBuilder(
        private val dataList: List<ExportMetadata>
    ) {
        fun build(): FileSpec {
            val routerInterface = ClassName("me.xx2bab.koncat.sample", "Markers")
            val list = ClassName("kotlin.collections", "List")
            val listOfString = list.parameterizedBy(String::class.asTypeName())
            val exportAPIs = dataList.flatMap { it.markers }
                .map { "\"$it\"" }
                .joinToString(separator = ", ")

            val getExportAPIListFunSpec = FunSpec.builder("getClassMarkerList").apply {
                returns(listOfString)
//                addModifiers(KModifier.OVERRIDE)
                addStatement("return listOf($exportAPIs)")
            }.build()
            return FileSpec.builder("me.xx2bab.koncat.sample", "Markers")
                .addType(
                    TypeSpec.classBuilder("Markers")
                        .addFunction(getExportAPIListFunSpec)
                        .build()
                )
                .build()
        }
    }

}

internal fun OutputStream.appendText(str: String) {
    this.write(str.toByteArray())
}
