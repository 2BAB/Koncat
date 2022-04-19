package me.xx2bab.koncat.gradle

import me.xx2bab.koncat.gradle.base.KoncatAndroidPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

class KoncatAndroidLibraryPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // Apply KoncatAndroidPlugin for common logic like extension/processor arguments
        project.apply<KoncatAndroidPlugin>()
    }

}