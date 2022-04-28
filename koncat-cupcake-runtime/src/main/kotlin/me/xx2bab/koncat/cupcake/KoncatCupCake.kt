package me.xx2bab.koncat.cupcake

import kotlin.reflect.KClass


class KoncatCupCake() {

    fun getAnnotatedClasses(annotation: KClass<out Annotation>): List<ClassDeclarationRecord>? =
        annotatedClasses[annotation]

    fun <T: Any> getInterfaceImplementations(interfaze: KClass<T>): List<() -> T>? =
        interfaceImplementations[interfaze] as? List<() -> T>

    fun <T: Any> getTypedProperties(type: KClass<T>): List<T>? =
        typedProperties[type] as? List<T>

}

