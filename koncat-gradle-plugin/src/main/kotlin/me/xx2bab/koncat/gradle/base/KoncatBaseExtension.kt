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
    /**
     * To declare current working project (Gradle module in another word) as Main Project,
     * the Main Project will collect all Koncat metadata from dependencies.
     */
    val declaredAsMainProject: Property<Boolean> = objects.property<Boolean>().convention(false)

    /**
     * To enable/disable the Aggregation Class generation.
     * The Aggregation Class is actually `me.xx2bab.koncat.runtime.KoncatAggregation`,
     * that will be used by `koncat-runtime` library in runtime,
     * to replace the `koncat-stub` one which is an empty & compile-only placeholder.
     */
    val generateAggregationClass: Property<Boolean> = objects.property<Boolean>().convention(true)

    /**
     * To enable/disable the Extension Class generation.
     * The Extension Class is actually `me.xx2bab.koncat.runtime.KoncatAggregatedMeta`,
     * that will be used by 3rd party developers to customize the process of aggregated metadata.
     * For example, to generate a custom Aggregation Class, or to generate an API/Route report
     * during compile time.
     */
    val generateExtensionClass: Property<Boolean> = objects.property<Boolean>().convention(false)

    /**
     * To specify classes that annotated by the annotation list below should be aggregated.
     * Anonymous classes are not supported.
     */
    val annotations: ListProperty<String> = objects.listProperty()

    /**
     * To specify top-level classes that extend or implement from supertype list below
     * should be aggregated. Indirect type search are supported.
     * For example, `android.app.Activity` is passed into [classTypes],
     * in your project `BaseActivity` is the wrapper for `Activity`,
     * then one of its implementation `MainActivity` which extends `BaseActivity` will be aggregated still.
     */
    val classTypes: ListProperty<String> = objects.listProperty()

    /**
     * To specify top-level properties that are declared as one of the type list below
     * should be aggregated. Indirect type search are supported.
     */
    val propertyTypes: ListProperty<String> = objects.listProperty()

    internal val mainProjectOutputDir = layout.buildDirectory
        .dir("intermediates")
        .map { it.dir("koncat") }

}