package me.xx2bab.koncat.processor.property

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.asClassName
import me.xx2bab.koncat.api.KoncatProcessorSupportAPI
import me.xx2bab.koncat.contract.KLogger
import me.xx2bab.koncat.processor.KSVisitorWithExportMetadata
import me.xx2bab.koncat.processor.KoncatProcMetadata
import me.xx2bab.koncat.processor.base.ClassNameAndType
import me.xx2bab.koncat.processor.base.SubProcessor
import kotlin.reflect.KClass

class PropertyTypeSubProcessor : SubProcessor {

    override fun onProcess(
        resolver: Resolver,
        koncat: KoncatProcessorSupportAPI,
        exportMetadata: KoncatProcMetadata,
        logger: KLogger
    ) {
        koncat.getTargetPropertyTypes().forEach {
            exportMetadata.typedProperties[it] = mutableListOf()
        }
        val targetPropTypeDeclarations = koncat.getTargetPropertyTypes().map {
            ClassNameAndType(
                it,
                resolver.getClassDeclarationByName(it)!!.asStarProjectedType()
            ) // May throw exceptions
        }
        resolver.getAllFiles().forEach {
            val visitor = PropertyBindingVisitor(targetPropTypeDeclarations, logger, koncat)
            it.accept(visitor, exportMetadata)
        }
    }

    inner class PropertyBindingVisitor(
        private val targetProperties: List<ClassNameAndType>,
        private val logger: KLogger,
        private val koncat: KoncatProcessorSupportAPI
    ) : KSVisitorWithExportMetadata() {

        override fun visitFile(file: KSFile, data: KoncatProcMetadata) {
            file.declarations.forEach { it.accept(this, data) }
        }

        override fun visitClassDeclaration(
            classDeclaration: KSClassDeclaration,
            data: KoncatProcMetadata
        ) {
        }

        override fun visitPropertyDeclaration(
            property: KSPropertyDeclaration,
            data: KoncatProcMetadata
        ) {
            val propertyName = property.qualifiedName?.asString()
            if (propertyName.isNullOrBlank()) { // DO NOT support anonymous classes
                return
            }
            logger.info("[PropertyTypeSubProcessor] Checking super types of $propertyName")

            val propertyKSType = property.type.resolve()
            targetProperties.forEach { targetProperty ->
                if (propertyKSType.isAssignableFrom(targetProperty.type)) {
                    logger.info("[PropertyTypeSubProcessor] Matched \"$propertyName\" on type \"${targetProperty}\"")
                    data.typedProperties[targetProperty.canonicalName]?.add(propertyName)
                    property.containingFile?.let { data.mapKSFiles.add(it) }
                }
            }
        }

    }

    override fun onGenerate(
        mergedMetadata: KoncatProcMetadata,
        logger: KLogger
    ): PropertySpec {
        val properties = mergedMetadata.typedProperties

        val keyType = KClass::class.asClassName().parameterizedBy(
            WildcardTypeName.producerOf(ANY)
        )
        val valueType = List::class.asClassName().parameterizedBy(ANY)
        val typedPropertiesPropertyParameterizedType = Map::class.asClassName().parameterizedBy(
            keyType, valueType
        )

        val propStringBuilder = StringBuilder()
        properties.forEach { (clazz, recordList) ->
            propStringBuilder.append("$clazz::class to listOf(")
            recordList.forEach { record ->
                propStringBuilder.append("$record, ")
            }
            propStringBuilder.append("), ")
        }

        return PropertySpec.builder("typedProperties", typedPropertiesPropertyParameterizedType)
            .initializer("mapOf($propStringBuilder)")
            .build()
    }

}