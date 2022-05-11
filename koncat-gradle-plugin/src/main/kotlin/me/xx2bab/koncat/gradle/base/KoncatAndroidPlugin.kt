package me.xx2bab.koncat.gradle.base

import me.xx2bab.koncat.contract.KSP_PLUGIN_NAME
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

class KoncatAndroidPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.apply<KoncatBasePlugin>()

        project.afterEvaluate {
            project.plugins.findPlugin(KSP_PLUGIN_NAME)?.run {
                project.tasks.findByPath("preBuild")?.let { preBuild ->
                    project.tasks.named(KoncatBasePlugin.BASE_ARGUMENTS_CONTRACT_GEN_TASK)
                        .configure { dependsOn(preBuild) }
                }
            }
        }

        // TODO: add a set of tasks for variant configuration
    }

}