package me.xx2bab.koncat.gradle.kcp

import me._bab.koncat_gradle_plugin.BuildConfig
import me.xx2bab.koncat.contract.DEFAULT_COMPILER_PLUGIN_ID
import me.xx2bab.koncat.gradle.base.KoncatBaseExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

class KCPDefaultGradlePlugin : KotlinCompilerPluginSupportPlugin {

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        val baseExt = project.extensions.getByType<KoncatBaseExtension>()
        return project.provider {
            baseExt.argumentsContract(project)
                .toMap()
                .map { SubpluginOption(it.key, it.value) }
        }
    }

    override fun getCompilerPluginId(): String = DEFAULT_COMPILER_PLUGIN_ID

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = "me.2bab",
        artifactId = DEFAULT_COMPILER_PLUGIN_ID,
        version = BuildConfig.KONCAT_VERSION
    )

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        val baseExt = kotlinCompilation.target.project.extensions.getByType<KoncatBaseExtension>()
        return baseExt.kcp.enabled.get()
    }

}