package me.xx2bab.koncat.gradle.base

import com.google.devtools.ksp.gradle.KspExtension
import me._bab.koncat_gradle_plugin.BuildConfig
import me.xx2bab.koncat.contract.KoncatArgumentsContract
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create

class KoncatBasePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // Set up the configuration
        val baseExt = project.extensions.create<KoncatBaseExtension>("koncat")

        // Set up annotation processor arguments
        project.afterEvaluate {
            baseExt.isMainProject.disallowChanges()
            val argumentsContract = KoncatArgumentsContract(
                projectName = project.name,
                koncatVersion = BuildConfig.KONCAT_VERSION,
                gradlePlugins = plugins.map { it.toString() },
                declaredAsMainProject = baseExt.isMainProject.get(),
                variantAwareIntermediates = baseExt.mainProjectOutputDir.get().asFile, // TODO: it may be consumed eagerly
            )
            project.plugins.findPlugin("com.google.devtools.ksp")?.run {
                project.extensions.configure<KspExtension> {
                    argumentsContract.toMap().forEach { k, v ->
                        arg(k, v)
                    }
                }
            }
        }
    }
}