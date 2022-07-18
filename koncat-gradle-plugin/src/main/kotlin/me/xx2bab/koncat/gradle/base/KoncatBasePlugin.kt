package me.xx2bab.koncat.gradle.base

import com.google.devtools.ksp.gradle.KspExtension
import com.google.devtools.ksp.gradle.KspTask
import me._bab.koncat_gradle_plugin.BuildConfig
import me.xx2bab.koncat.contract.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.process.CommandLineArgumentProvider
import java.io.File

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
            baseExt.declaredAsMainProject.disallowChanges()
            val argumentsContract = KoncatArgumentsContract(
                projectName = project.name,
                koncatVersion = BuildConfig.KONCAT_VERSION,
                gradlePlugins = plugins.map { it.toString().split("@")[0] },
                declaredAsMainProject = baseExt.declaredAsMainProject.get(),
                generateAggregationClass = baseExt.generateAggregationClass.get(),
                generateExtensionClass = baseExt.generateExtensionClass.get(),
                targetAnnotations = baseExt.annotations.get(),
                targetClassTypes = baseExt.classTypes.get(),
                targetPropertyTypes = baseExt.propertyTypes.get()
            )
            project.plugins.findPlugin(KSP_PLUGIN_NAME)?.run {
                class ConfigFileProvider(
                    @InputDirectory
                    @PathSensitive(PathSensitivity.RELATIVE)
                    val config: File
                ):CommandLineArgumentProvider {
                    override fun asArguments(): Iterable<String> {
                        return listOf("$KONCAT_ARGUMENT_INTERMEDIATES_DIR=${config.path}")
                    }
                }
                project.extensions.configure<KspExtension> {
                    val dir = baseExt.mainProjectOutputDir.get().asFile
                    arg(ConfigFileProvider(dir))
                }
                val genBaseArgsTask = project.tasks.register<GenerateArgumentsContractTask>(
                    BASE_ARGUMENTS_CONTRACT_GEN_TASK
                ) {
                    contractJson.set(argumentsContract.encodeKoncatArguments())
                    target.set(baseExt.mainProjectOutputDir.map {
                        it.file(KONCAT_ARGUMENT_TARGET_FILE_BASE)
                    })
                }
                project.tasks.withType<KspTask> {
                    dependsOn(genBaseArgsTask)
                }
            }
        }
    }


}