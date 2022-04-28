package me.xx2bab.koncat.processor

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
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
import kotlin.reflect.KClass

class CupcakeProcessorProvider : SymbolProcessorProvider {
    override fun create(
        env: SymbolProcessorEnvironment
    ): SymbolProcessor {
        return CupcakeProcessor(
            env.codeGenerator,
            env.logger,
            Koncat(KSPAdapter(env))
        )
    }
}

class CupcakeProcessor(
    val codeGenerator: CodeGenerator,
    val logger: KSPLogger,
    val koncat: Koncat
) : SymbolProcessor {

    companion object {
        private const val id = "-cupcake-proc"
    }

    private val exportMetadata = CupcakeProcMetadata()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.info("$LOG_TAG[process]")
        koncat.getTargetAnnotations()
            .forEach { annotation ->
                logger.info(LOG_TAG + "Process $annotation")
                exportMetadata.annotatedClasses.putIfAbsent(annotation, mutableListOf())

                resolver.getSymbolsWithAnnotation(annotation).filter { ksAnnotated ->
                    ksAnnotated is KSClassDeclaration && ksAnnotated.validate()
                }.forEach { ksAnnotated ->
                    logger.info(LOG_TAG + "Visit $ksAnnotated")
                    ksAnnotated.accept(
                        AnnotatedClassesVisitor(annotation),
                        exportMetadata
                    )
                }
            }
        resolver.getAllFiles().forEach {
            it.accept(InterfaceBindingVisitor(), exportMetadata)
        }
        return emptyList()
    }

    @OptIn(KotlinPoetKspPreview::class)
    override fun finish() {
        super.finish()
        logger.info("$LOG_TAG[finish]")
        if (koncat.isMainProject()) {
            logger.info(
                LOG_TAG + "Query all sub projects meta data from "
                        + koncat.getIntermediatesDir().absolutePath
            )
            // Merge all ExportMetadata
            val subProjectMetadataList = koncat.getIntermediatesFiles()
                .filter { it.name.contains(id) }
                .map { subProjectMetadataFile ->
                    logger.info(LOG_TAG + "Aggregate from ${subProjectMetadataFile.absolutePath}")
                    Json.decodeFromStream<CupcakeProcMetadata>(
                        subProjectMetadataFile.inputStream()
                    )
                }
            val all = mutableListOf<CupcakeProcMetadata>()
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

    inner class AnnotatedClassesVisitor(private val currentAnno: String) : KSVisitorWithExportMetadata() {
        override fun visitClassDeclaration(
            classDeclaration: KSClassDeclaration,
            data: CupcakeProcMetadata
        ) {
            val annotations = classDeclaration.annotations.map { anno ->
                val argumentMap = mutableMapOf<String, String>()
                anno.arguments.forEach { ksValueArgument ->
                    if (ksValueArgument.name != null) {
                        argumentMap[ksValueArgument.name!!.getShortName()] =
                            ksValueArgument.value.toString()
                    }
                }
                AnnotationRecord(
                    name = anno.annotationType.resolve().declaration.qualifiedName!!.asString(),
                    arguments = argumentMap
                )
            }.toList()
            val declarationRecord = ClassDeclarationRecord(
                classDeclaration.qualifiedName!!.asString(), // Do not support anonymous classes
                annotations
            )
            data.annotatedClasses[currentAnno]?.add(declarationRecord)
            classDeclaration.containingFile?.let { data.mapKSFiles.add(it) }
        }
    }

    inner class RouterClassBuilder(
        private val dataList: List<CupcakeProcMetadata>
    ) {
        fun build(): FileSpec {
            val annotatedClasses: MutableMap<String, MutableList<ClassDeclarationRecord>> =
                mutableMapOf()
            dataList.forEach {
                it.annotatedClasses.forEach { (key, value) ->
                    if (annotatedClasses.containsKey(key)) {
                        annotatedClasses[key]?.addAll(value)
                    } else {
                        annotatedClasses[key] = value
                    }
                }
            }


            val list = List::class.asClassName()
            val annotatedClassesPropertyType = Map::class.asClassName()
            val annotationKClassType = KClass::class.asClassName()
            val annotationKClassParameterizedType = annotationKClassType.parameterizedBy(
                WildcardTypeName.producerOf(Annotation::class.asTypeName())
            )
            val classRecordType = ClassName("me.xx2bab.koncat.cupcake", "ClassDeclarationRecord")
            val listOfClassRecordType = list.parameterizedBy(classRecordType)
            val annotatedClassesPropertyParameterizedType =
                annotatedClassesPropertyType.parameterizedBy(
                    annotationKClassParameterizedType,
                    listOfClassRecordType
                )

            val annoStringBuilder = StringBuilder()
            annotatedClasses.forEach { (clazz, recordList) ->
                annoStringBuilder.append("$clazz::class to listOf(")
                recordList.forEach { record ->
                    annoStringBuilder.append("ClassDeclarationRecord(")
                    annoStringBuilder.append("name = \"${record.name}\",")

                    annoStringBuilder.append("annotations = listOf(")
                    record.annotations.forEach { anno ->
                        annoStringBuilder.append("AnnotationRecord(name = \"${anno.name}\", ")
                        annoStringBuilder.append("arguments = mapOf(")
                        anno.arguments.forEach { (aKey, aValue) ->
                            annoStringBuilder.append("\"$aKey\" to \"$aValue\", ")
                        }
                        annoStringBuilder.append(")),")
                    }

                    annoStringBuilder.append(")),")
                }
                annoStringBuilder.append("),")
            }

            return FileSpec.builder("me.xx2bab.koncat.cupcake", "KoncatCupCakeAggregation")
                .addProperty(
                    PropertySpec.builder(
                        "annotatedClasses",
                        annotatedClassesPropertyParameterizedType
                    )
                        .initializer("mapOf($annoStringBuilder)")
                        .build()
                )
                .build()
        }
    }

    inner class InterfaceBindingVisitor() : KSVisitorWithExportMetadata() {
        override fun visitClassDeclaration(
            classDeclaration: KSClassDeclaration,
            data: CupcakeProcMetadata
        ) {
            logger.info("checking supertypes of " + classDeclaration.qualifiedName?.asString())
            classDeclaration.getAllSuperTypes().forEach {
                logger.info(it.declaration.qualifiedName?.asString() ?: it.declaration.simpleName.asString())
            }
        }

        override fun visitFile(file: KSFile, data: CupcakeProcMetadata) {
            file.declarations.forEach { it.accept(this, data) }
        }
    }

}

internal fun OutputStream.appendText(str: String) {
    this.write(str.toByteArray())
}
