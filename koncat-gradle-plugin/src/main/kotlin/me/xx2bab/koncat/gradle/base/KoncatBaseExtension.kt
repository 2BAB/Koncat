package me.xx2bab.koncat.gradle.base

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
    val declaredAsMainProject: Property<Boolean> = objects.property<Boolean>().convention(false)

    val extendable: Property<Boolean> = objects.property<Boolean>().convention(false)

    val annotations: ListProperty<String> = objects.listProperty()

    val interfaces: ListProperty<String> = objects.listProperty()

    val properties: ListProperty<String> = objects.listProperty()

    internal val mainProjectOutputDir = layout.buildDirectory
        .dir("intermediates")
        .map { it.dir("koncat") }

}