package me.xx2bab.koncat.gradle.base

import com.google.devtools.ksp.gradle.KspExtension
import com.google.devtools.ksp.gradle.KspTask
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me._bab.koncat_gradle_plugin.BuildConfig
import me.xx2bab.koncat.contract.KONCAT_ARGUMENT_INTERMEDIATES_DIR
import me.xx2bab.koncat.contract.KONCAT_ARGUMENT_TARGET_FILE_BASE
import me.xx2bab.koncat.contract.KSP_PLUGIN_NAME
import me.xx2bab.koncat.contract.KoncatArgumentsContract
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

class KoncatBasePlugin : Plugin<Project> {

    companion object {
        const val BASE_ARGUMENTS_CONTRACT_GEN_TASK = "generateBaseKoncatArguments"
        const val VARIANT_AWARE_ARGUMENTS_CONTRACT_GEN_TASK = "generate%sKoncatArguments"
    }

    override fun apply(project: Project) {
        // Set up the configuration
        val baseExt = project.extensions.create<KoncatBaseExtension>("koncat")

        // Set up annotation processor arguments
        project.afterEvaluate {
            baseExt.isMainProject.disallowChanges()
            val argumentsContract = KoncatArgumentsContract(
                projectName = project.name,
                koncatVersion = BuildConfig.KONCAT_VERSION,
                gradlePlugins = plugins.map { it.toString().split("@")[0] },
                declaredAsMainProject = baseExt.isMainProject.get()
            )
            project.plugins.findPlugin(KSP_PLUGIN_NAME)?.run {
                project.extensions.configure<KspExtension> {
                    val dir = baseExt.mainProjectOutputDir.get().asFile
                    arg(KONCAT_ARGUMENT_INTERMEDIATES_DIR, dir.absolutePath)
                }
                val genBaseArgsTask = project.tasks.register<GenerateArgumentsContractTask>(
                    BASE_ARGUMENTS_CONTRACT_GEN_TASK
                ) {
                    contractJson.set(Json.encodeToString(argumentsContract))
                    target.set(baseExt.mainProjectOutputDir.map {
                        it.file(KONCAT_ARGUMENT_TARGET_FILE_BASE)
                    })
                }
                project.tasks.withType<KspTask> {
                    inputs.files(genBaseArgsTask.flatMap { it.target })
                }
            }
        }
    }


}