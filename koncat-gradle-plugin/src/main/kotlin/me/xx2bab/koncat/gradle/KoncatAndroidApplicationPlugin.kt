package me.xx2bab.koncat.gradle

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import me.xx2bab.koncat.gradle.base.KoncatAndroidPlugin
import me.xx2bab.koncat.gradle.base.KoncatBaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.getByType

class KoncatAndroidApplicationPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // Apply KoncatAndroidPlugin for common logic like extension/processor arguments
        project.apply<KoncatAndroidPlugin>()
        val baseExt = project.extensions.getByType<KoncatBaseExtension>()
        baseExt.declaredAsMainProject.set(true)

        val androidExtension = project.extensions
            .getByType(ApplicationAndroidComponentsExtension::class.java)

        // Prevent all Koncat intermediates from being packaged into the final package(.apk/.aab).
        androidExtension.finalizeDsl {
            it.packagingOptions.resources.excludes += "**/*.koncat"
        }
    }

}