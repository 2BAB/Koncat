package me.xx2bab.koncat.processor.anno

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import me.xx2bab.koncat.api.KoncatProcessorSupportAPI
import me.xx2bab.koncat.contract.KLogger
import me.xx2bab.koncat.processor.AnnotationRecord
import me.xx2bab.koncat.processor.ClassDeclarationRecord
import me.xx2bab.koncat.processor.KSVisitorWithExportMetadata
import me.xx2bab.koncat.processor.KoncatProcMetadata
import me.xx2bab.koncat.processor.base.SubProcessor
import kotlin.reflect.KClass

class AnnotationSubProcessor : SubProcessor {

    override fun onProcess(
        resolver: Resolver,
        koncat: KoncatProcessorSupportAPI,
        exportMetadata: KoncatProcMetadata,
        logger: KLogger
    ) {
        koncat.getTargetAnnotations()
            .forEach { annotation ->
                logger.info("[AnnotationBasedSubProcessor] Process $annotation")
                exportMetadata.annotatedClasses.putIfAbsent(annotation, mutableListOf())

                resolver.getSymbolsWithAnnotation(annotation).filter { ksAnnotated ->
                    ksAnnotated is KSClassDeclaration && ksAnnotated.validate()
                }.forEach { ksAnnotated ->
                    logger.info("[AnnotationBasedSubProcessor] Visit $ksAnnotated")
                    ksAnnotated.accept(
                        AnnotatedClassesVisitor(annotation),
                        exportMetadata
                    )
                }
            }
    }

    inner class AnnotatedClassesVisitor(private val currentAnno: String) :
        KSVisitorWithExportMetadata() {
        override fun visitClassDeclaration(
            classDeclaration: KSClassDeclaration,
            data: KoncatProcMetadata
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

    override fun onGenerate(mergedMetadata: KoncatProcMetadata,
                            logger: KLogger): PropertySpec {
        val annotatedClasses = mergedMetadata.annotatedClasses

        val list = List::class.asClassName()
        val annotatedClassesPropertyType = Map::class.asClassName()
        val annotationKClassType = KClass::class.asClassName()
        val annotationKClassParameterizedType = annotationKClassType.parameterizedBy(
            WildcardTypeName.producerOf(Annotation::class.asTypeName())
        )
        val classRecordType = ClassName("me.xx2bab.koncat.runtime", "ClassDeclarationRecord")
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

        return PropertySpec.builder("annotatedClasses", annotatedClassesPropertyParameterizedType)
            .initializer("mapOf($annoStringBuilder)")
            .build()
    }

}