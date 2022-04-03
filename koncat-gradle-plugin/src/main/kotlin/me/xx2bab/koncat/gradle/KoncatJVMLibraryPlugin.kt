package me.xx2bab.koncat.gradle

import me.xx2bab.koncat.gradle.base.KoncatBasePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

class KoncatJVMLibraryPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // Apply KoncatBasePlugin for common logic like extension/processor arguments
        project.apply<KoncatBasePlugin>()
    }

}