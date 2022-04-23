package me.xx2bab.koncat.sample.kotlin

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.writeTo
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import me.xx2bab.koncat.api.Koncat
import me.xx2bab.koncat.api.adapter.KSPAdapter
import me.xx2bab.koncat.contract.KONCAT_FILE_EXTENSION
import java.io.OutputStream

class ExportProcessorProvider : SymbolProcessorProvider {
    override fun create(
        env: SymbolProcessorEnvironment
    ): SymbolProcessor {
        return ExportProcessor(
            env.codeGenerator,
            env.logger,
            Koncat(KSPAdapter(env))
        )
    }
}

class ExportProcessor(
    val codeGenerator: CodeGenerator,
    val logger: KSPLogger,
    val koncat: Koncat
) : SymbolProcessor {

    companion object {
        private const val id = "-export-api-proc"
    }

    private var exportMetadata = ExportMetadata()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(
            "me.xx2bab.koncat.sample.annotation.ExportAPI"
        )
        val ret = symbols.filter { !it.validate() }.toList()
        symbols.filter { it is KSClassDeclaration && it.validate() }
            .forEach { it.accept(BuilderVisitor(), exportMetadata) }
        return ret
    }

    @OptIn(KotlinPoetKspPreview::class)
    override fun finish() {
        super.finish()
        if (koncat.isMainProject()) {
            // Merge all ExportMetadata
            val subProjectMetadataList = koncat.getIntermediatesFiles()
                .filter { it.name.contains(id) }
                .map { subProjectMetadataFile ->
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

    inner class BuilderVisitor() : KSVisitorWithExportMetadata() {
        override fun visitClassDeclaration(
            classDeclaration: KSClassDeclaration,
            data: ExportMetadata
        ) {
            data.exportAPIs.add(classDeclaration.qualifiedName!!.asString())
            classDeclaration.containingFile?.let { data.mapKSFiles.add(it) }
        }
    }

    inner class RouterClassBuilder(
        private val dataList: List<ExportMetadata>
    ) {
        fun build(): FileSpec {
            val routerInterface = ClassName("me.xx2bab.koncat.sample", "ExportCapabilityRouter")
            val list = ClassName("kotlin.collections", "List")
            val listOfString = list.parameterizedBy(String::class.asTypeName())
            val exportAPIs = dataList.flatMap { it.exportAPIs }
                .map { "\"$it\"" }
                .joinToString(separator = ", ")

            val getExportAPIListFunSpec = FunSpec.builder("getExportAPIList").apply {
                returns(listOfString)
                addModifiers(KModifier.OVERRIDE)
                addStatement("return listOf($exportAPIs)")
            }.build()
            return FileSpec.builder("me.xx2bab.koncat.sample", "ExportCapabilityRouterImpl")
                .addType(
                    TypeSpec.classBuilder("ExportCapabilityRouterImpl")
                        .addSuperinterface(routerInterface)
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

@Serializable
data class ExportMetadata(
    val exportAPIs: MutableList<String> = mutableListOf(),
    @Transient val mapKSFiles: MutableList<KSFile> = mutableListOf()
)