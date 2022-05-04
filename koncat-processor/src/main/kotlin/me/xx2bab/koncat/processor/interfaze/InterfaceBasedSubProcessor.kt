package me.xx2bab.koncat.processor.interfaze

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.asClassName
import me.xx2bab.koncat.api.KoncatProcessorSupportAPI
import me.xx2bab.koncat.contract.KLogger
import me.xx2bab.koncat.processor.KSVisitorWithExportMetadata
import me.xx2bab.koncat.processor.KoncatProcMetadata
import me.xx2bab.koncat.processor.base.SubProcessor
import kotlin.reflect.KClass

class InterfaceBasedSubProcessor : SubProcessor {

    override fun onProcess(
        resolver: Resolver,
        koncat: KoncatProcessorSupportAPI,
        exportMetadata: KoncatProcMetadata,
        logger: KLogger
    ) {
        koncat.getTargetInterfaces().forEach {
            exportMetadata.interfaces[it] = mutableListOf()
        }
        resolver.getAllFiles().forEach {
            val visitor = InterfaceBindingVisitor(koncat.getTargetInterfaces(), logger)
            it.accept(visitor, exportMetadata)
        }
    }

    inner class InterfaceBindingVisitor(
        private val targetInterfaces: List<String>,
        private val logger: KLogger
    ) : KSVisitorWithExportMetadata() {

        override fun visitFile(file: KSFile, data: KoncatProcMetadata) {
            file.declarations.forEach { it.accept(this, data) }
        }

        override fun visitClassDeclaration(
            classDeclaration: KSClassDeclaration,
            data: KoncatProcMetadata
        ) {
            val className = classDeclaration.qualifiedName?.asString()
            if (className.isNullOrBlank()) { // DO NOT support anonymous classes
                return
            }
            logger.info("[InterfaceBasedSubProcessor] Checking supertypes of " + classDeclaration.qualifiedName?.asString())
            classDeclaration.getAllSuperTypes().forEach { superType ->
                val superTypeClassName = superType.declaration.qualifiedName
                if (superTypeClassName != null) {
                    targetInterfaces.forEach { targetInterface ->
                        if (superTypeClassName.asString() == targetInterface) {
                            logger.info("[InterfaceBasedSubProcessor] Matched \"${classDeclaration.qualifiedName!!.asString()}\" on type \"${targetInterface}\"")
                            data.interfaces[targetInterface]?.add(className)
                            classDeclaration.containingFile?.let { data.mapKSFiles.add(it) }
                        }
                    }
                }
            }
        }

    }

    override fun onGenerate(mergedMetadata: KoncatProcMetadata,
                            logger: KLogger): PropertySpec {
        val interfaces = mergedMetadata.interfaces

        val keyType = KClass::class.asClassName().parameterizedBy(
            WildcardTypeName.producerOf(ANY)
        )
        val valueType = List::class.asClassName().parameterizedBy(ANY)
        val interfacesPropertyParameterizedType = Map::class.asClassName().parameterizedBy(
            keyType, valueType)

        val intfStringBuilder = StringBuilder()
        interfaces.forEach { (clazz, recordList) ->
            intfStringBuilder.append("$clazz::class to listOf(")
            recordList.forEach { record ->
                intfStringBuilder.append("{$record()}, ")
            }
            intfStringBuilder.append("), ")
        }

        return PropertySpec.builder("interfaceImplementations", interfacesPropertyParameterizedType)
            .initializer("mapOf($intfStringBuilder)")
            .build()
    }

}