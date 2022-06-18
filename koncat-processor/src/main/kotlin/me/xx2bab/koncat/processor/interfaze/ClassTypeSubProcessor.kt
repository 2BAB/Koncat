package me.xx2bab.koncat.processor.interfaze

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.toClassName
import me.xx2bab.koncat.api.KoncatProcAPI
import me.xx2bab.koncat.api.KoncatProcMetadata
import me.xx2bab.koncat.contract.KLogger
import me.xx2bab.koncat.processor.KSVisitorWithExportMetadata
import me.xx2bab.koncat.processor.base.ClassNameAndType
import me.xx2bab.koncat.processor.base.SubProcessor
import kotlin.reflect.KClass

class ClassTypeSubProcessor(
    private val koncat: KoncatProcAPI,
    private val exportMetadata: KoncatProcMetadata,
    private val logger: KLogger) : SubProcessor {

    init {
        koncat.getTargetClassTypes().forEach {
            exportMetadata.typedClasses[it] = mutableListOf()
        }
    }

    override fun onProcess(
        resolver: Resolver
    ): List<KSAnnotated> {
        val targetClassTypeDeclarations = koncat.getTargetClassTypes().map {
            ClassNameAndType(
                it,
                try {
                resolver.getClassDeclarationByName(it)!!.asStarProjectedType()
                } catch (e: NullPointerException) {
                    logger.error("${koncat.projectName} can not resolve $it from classpath.")
                    throw e
                }
            ) // May throw exceptions if the classpath of target class types are not existed.
        }
        resolver.getNewFiles().forEach {
            val visitor = InterfaceBindingVisitor(targetClassTypeDeclarations, logger, koncat)
            it.accept(visitor, exportMetadata)
        }

        return emptyList()
    }

    inner class InterfaceBindingVisitor(
        private val targetInterfaces: List<ClassNameAndType>,
        private val logger: KLogger,
        private val koncat: KoncatProcAPI
    ) : KSVisitorWithExportMetadata() {

        override fun visitFile(file: KSFile, data: KoncatProcMetadata) {
            file.declarations.forEach { it.accept(this, data) }
        }

        @OptIn(KotlinPoetKspPreview::class)
        override fun visitClassDeclaration(
            classDeclaration: KSClassDeclaration,
            data: KoncatProcMetadata,
        ) {
            val className = classDeclaration.qualifiedName?.asString()
            if (className.isNullOrBlank()) { // DO NOT support anonymous classes
                return
            }
            logger.info("[ClassTypeSubProcessor] Checking supertypes of " + classDeclaration.qualifiedName?.asString())
            classDeclaration.superTypes.forEach { superType ->
                val superKSType = superType.resolve()
                if (superKSType.toClassName().canonicalName == "kotlin.Any") {
                    return@forEach
                }
                targetInterfaces.forEach { targetInterface ->
                    if (superKSType.isAssignableFrom(targetInterface.type)) {
                        logger.info("[ClassTypeSubProcessor] Matched \"${classDeclaration.qualifiedName!!.asString()}\" on type \"${targetInterface}\"")
                        data.typedClasses[targetInterface.canonicalName]?.add(className)
                        classDeclaration.containingFile?.let { data.mapKSFiles.add(it) }
                    }
                }
            }
        }

    }

    override fun onGenerate(
        mergedMetadata: KoncatProcMetadata,
        logger: KLogger
    ): PropertySpec {
        val interfaces = mergedMetadata.typedClasses

        val keyType = KClass::class.asClassName().parameterizedBy(
            WildcardTypeName.producerOf(ANY)
        )
        val valueType = List::class.asClassName().parameterizedBy(ANY)
        val interfacesPropertyParameterizedType = Map::class.asClassName().parameterizedBy(
            keyType, valueType
        )

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