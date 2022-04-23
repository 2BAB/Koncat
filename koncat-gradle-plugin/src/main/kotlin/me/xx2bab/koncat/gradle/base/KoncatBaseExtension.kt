package me.xx2bab.koncat.gradle.base

import org.gradle.api.Action
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property
import javax.inject.Inject


abstract class KoncatBaseExtension @Inject constructor(
    objects: ObjectFactory,
    layout: ProjectLayout
) {
    val isMainProject: Property<Boolean> = objects.property<Boolean>().convention(false)

    fun defaultProcessor(action: Action<DefaultProcessor>) {
        action.execute(defaultProcessor)
    }
    val defaultProcessor = objects.newInstance(DefaultProcessor::class.java)

    internal val mainProjectOutputDir = layout.buildDirectory
        .dir("intermediates")
        .map { it.dir("koncat") }

}

abstract class DefaultProcessor @Inject constructor(
    objects: ObjectFactory
) {
    val enabled: Property<Boolean> = objects.property<Boolean>().convention(true)

    val annotations: ListProperty<String> = objects.listProperty()
    
    val interfaces: ListProperty<String> = objects.listProperty()

}