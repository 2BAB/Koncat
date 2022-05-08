package me.xx2bab.koncat.gradle

import me.xx2bab.koncat.gradle.base.KoncatAndroidPlugin
import me.xx2bab.koncat.gradle.base.KoncatBaseExtension
import me.xx2bab.polyfill.PolyfillPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.getByType

class KoncatAndroidApplicationPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // Set up Polyfill to provide additional Artifacts
        project.apply<PolyfillPlugin>()
        // Apply KoncatAndroidPlugin for common logic like extension/processor arguments
        project.apply<KoncatAndroidPlugin>()
        val baseExt = project.extensions.getByType<KoncatBaseExtension>()
        baseExt.declaredAsMainProject.set(true)

//        val androidExtension = project.extensions
//            .getByType(ApplicationAndroidComponentsExtension::class.java)

        // Prevent all Koncat intermediates from being packaged into the final package(.apk/.aab).
//        androidExtension.finalizeDsl { appExt ->
//
//        }
    }

}