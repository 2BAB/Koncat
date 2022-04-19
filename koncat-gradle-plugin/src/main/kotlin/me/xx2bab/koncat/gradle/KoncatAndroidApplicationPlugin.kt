package me.xx2bab.koncat.gradle

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.gradle.internal.tasks.factory.dependsOn
import me.xx2bab.koncat.contract.AGGREGATION_ERROR
import me.xx2bab.koncat.contract.KONCAT_FILE_EXTENSION
import me.xx2bab.koncat.contract.LOG_TAG
import me.xx2bab.koncat.gradle.base.KoncatAndroidPlugin
import me.xx2bab.koncat.gradle.base.KoncatBaseExtension
import me.xx2bab.polyfill.*
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

class KoncatAndroidApplicationPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // Set up Polyfill to provide additional Artifacts
        project.apply<PolyfillPlugin>()
        // Apply KoncatAndroidPlugin for common logic like extension/processor arguments
        project.apply<KoncatAndroidPlugin>()
        val baseExt = project.extensions.getByType<KoncatBaseExtension>()
        baseExt.isMainProject.set(true)

        val androidExtension = project.extensions
            .getByType(ApplicationAndroidComponentsExtension::class.java)

        // Prevent all Koncat intermediates from being packaged into the final package(.apk/.aab).
        androidExtension.finalizeDsl {
            it.packagingOptions.resources.excludes += "**/*.koncat"
        }

        // Set up Koncat aggregation flow.
//        val variantAwarePropertiesIndex = baseExt.mainProjectOutputDir.map { it.file("index.properties") }
//        val prepareEnvTaskProvider = project.tasks.register<PrepareKoncatAggregationEnv>(
//            "prepareKoncatAggregationEnv") {
//            variantAwarePropertiesIndexFileProvider.set(variantAwarePropertiesIndex)
//        }
        androidExtension.onVariants { variant ->
            val variantAwareAggregatedOutput = baseExt.mainProjectOutputDir.map { it.dir(variant.name) }
            val aggregateTaskProvider = project.tasks.register<AggregateKoncatIntermediatesTask>(
                "aggregateKoncatFilesFor${variant.getCapitalizedName()}") {
                inputJavaResFiles.set(variant.artifactsPolyfill
                    .getAll(PolyfilledMultipleArtifact.ALL_JAVA_RES))
                aggregateDir.set(variantAwareAggregatedOutput)
            }
            project.afterEvaluate {
                aggregateTaskProvider.dependsOn(variant.getTaskContainer().preBuildTask)
                variant.getTaskContainer().processJavaResourcesTask.dependsOn(aggregateTaskProvider)
                tasks.findByPath("ksp${variant.getCapitalizedName()}Kotlin")
                    ?.dependsOn(aggregateTaskProvider)
            }
        }

    }

//    abstract class PrepareKoncatAggregationEnv: DefaultTask() {
//        @get: InputFile
//        abstract val variantAwarePropertiesIndexFileProvider: Property<RegularFile>
//    }

    abstract class AggregateKoncatIntermediatesTask : DefaultTask() {

        @get:InputFiles
        abstract val inputJavaResFiles: ListProperty<RegularFile>

        @get:OutputDirectory
        abstract val aggregateDir: DirectoryProperty

        @TaskAction
        fun aggregate() {
            val out = aggregateDir.get().asFile
            out.mkdirs()
            inputJavaResFiles.get().forEach { resFile ->
                try {
                    extractFileByExtensionFromZip(resFile.asFile, KONCAT_FILE_EXTENSION, out)
                } catch (e: Exception) {
                    logger.error(LOG_TAG + AGGREGATION_ERROR, resFile.asFile.name)
                }
            }
        }
    }

}