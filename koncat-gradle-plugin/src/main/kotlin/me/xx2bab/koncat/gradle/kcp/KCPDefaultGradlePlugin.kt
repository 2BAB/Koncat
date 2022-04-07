package me.xx2bab.koncat.gradle.kcp

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
        val baseExt = kotlinCompilation.target.project.extensions.getByType<KoncatBaseExtension>()
        return kotlinCompilation.target.project.provider {
//            baseExt.contract!!.toMap().map {
//                SubpluginOption(it.key, it.value)
//            }
            listOf()
        }
    }

    override fun getCompilerPluginId(): String = DEFAULT_COMPILER_PLUGIN_ID

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = "me.2bab",
        artifactId = "koncat-default-compiler-plugin",
        version = "1.1.0"
    )

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        val baseExt = kotlinCompilation.target.project.extensions.getByType<KoncatBaseExtension>()
        return baseExt.kcp.enabled.get()
    }

}