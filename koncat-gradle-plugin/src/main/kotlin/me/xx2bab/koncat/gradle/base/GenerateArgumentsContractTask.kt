package me.xx2bab.koncat.gradle.base

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * A helper task to generate base/variant-aware processor arguments.
 */
abstract class GenerateArgumentsContractTask : DefaultTask() {

    @get:Input
    abstract val contractJson: Property<String>

    @get:OutputFile
    abstract val target: RegularFileProperty

    @TaskAction
    fun generateBaseContract() {
        val targetFile = target.asFile.get()
        targetFile.parentFile.mkdirs()
        targetFile.createNewFile()
        targetFile.writeText(contractJson.get())
    }

}