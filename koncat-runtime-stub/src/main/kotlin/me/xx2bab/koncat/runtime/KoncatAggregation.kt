package me.xx2bab.koncat.runtime

import kotlin.reflect.KClass

val annotatedClasses = mapOf<KClass<out Annotation>,
        List<ClassDeclarationRecord>>()

val interfaceImplementations = mapOf<KClass<*>, Any>()

val typedProperties = mapOf<KClass<*>, Any>()