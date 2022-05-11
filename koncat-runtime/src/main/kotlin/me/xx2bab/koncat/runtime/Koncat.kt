package me.xx2bab.koncat.runtime

import kotlin.reflect.KClass

class Koncat {

    fun getAnnotatedClasses(annotation: KClass<out Annotation>): List<ClassDeclarationRecord>? =
        annotatedClasses[annotation]

    fun <T : Any> getTypedClasses(interfaze: KClass<T>): List<() -> T>? =
        interfaceImplementations[interfaze] as? List<() -> T>

    fun <T : Any> getTypedProperties(type: KClass<T>): List<T>? =
        typedProperties[type] as? List<T>

}

