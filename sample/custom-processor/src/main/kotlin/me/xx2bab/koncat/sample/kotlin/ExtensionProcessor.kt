package me.xx2bab.koncat.sample.kotlin

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.writeTo
import me.xx2bab.koncat.api.KoncatProcAPI
import me.xx2bab.koncat.api.KoncatProcAPIImpl
import me.xx2bab.koncat.api.KoncatProcMetadata
import me.xx2bab.koncat.api.KoncatProcMetadataHolder
import me.xx2bab.koncat.api.adapter.KSPAdapter

class ExtensionProcessorProvider : SymbolProcessorProvider {
    override fun create(
        environment: SymbolProcessorEnvironment
    ): SymbolProcessor {
        environment.options.forEach { entry ->
            environment.logger.info("[option]: ${entry.key} - ${entry.value}")
        }
        return ExtensionProcessor(
            environment.codeGenerator,
            environment.logger,
            KoncatProcAPIImpl(KSPAdapter(environment))
        )
    }
}

class ExtensionProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val koncat: KoncatProcAPI
) : SymbolProcessor {

    private var holder: KoncatProcMetadataHolder? = null

    @OptIn(KotlinPoetKspPreview::class)
    override fun process(resolver: Resolver): List<KSAnnotated> {
        holder = koncat.syncAggregatedMetadata(resolver)
        return emptyList()
    }

    @OptIn(KotlinPoetKspPreview::class)
    override fun finish() {
        super.finish()
        holder?.apply {
            val fileSpec = RouterClassBuilder(resolve()).build()
            fileSpec.writeTo(codeGenerator, Dependencies(false, dependency))
        }
    }

    inner class RouterClassBuilder(
        private val data: KoncatProcMetadata
    ) {
        fun build(): FileSpec {
            val routerInterface = ClassName("me.xx2bab.koncat.sample", "CustomRouter")

            val listOfString = List::class.asTypeName().parameterizedBy(String::class.asTypeName())
            val exportAPIs = data.typedClasses["me.xx2bab.koncat.sample.interfaze.DummyAPI"]!!
                .joinToString(separator = ", ") { "\"$it\"" }
            val getExportAPIListFunSpec = FunSpec.builder("getDummyAPIList").apply {
                returns(listOfString)
                addModifiers(KModifier.OVERRIDE)
                addStatement("return listOf($exportAPIs)")
            }.build()

            val exportActivities = data.annotatedClasses["me.xx2bab.koncat.sample.annotation.ExportActivity"]
            val activityMapBody = exportActivities!!.map {
                "\"${it.annotations.first().arguments["uri"].toString()}\" to \"${it.name}\""
            }.joinToString(separator = ", ")
            val getActivityMapFunSpec = FunSpec.builder("getActivityMap").apply {
                returns(Map::class.asTypeName().parameterizedBy(String::class.asTypeName(),
                    String::class.asTypeName()))
                addModifiers(KModifier.OVERRIDE)
                addStatement("return mapOf($activityMapBody)")
            }.build()

            return FileSpec.builder("me.xx2bab.koncat.sample", "CustomRouterImpl")
                .addType(
                    TypeSpec.classBuilder("CustomRouterImpl")
                        .addSuperinterface(routerInterface)
                        .addFunction(getExportAPIListFunSpec)
                        .addFunction(getActivityMapFunSpec)
                        .build()
                )
                .build()
        }
    }
}
