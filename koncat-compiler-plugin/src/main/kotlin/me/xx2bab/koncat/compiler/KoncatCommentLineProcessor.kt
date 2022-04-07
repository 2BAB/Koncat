package me.xx2bab.koncat.compiler

import me.xx2bab.koncat.contract.DEFAULT_COMPILER_PLUGIN_ID
import me.xx2bab.koncat.contract.KoncatArgument
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

class KoncatCommentLineProcessor: CommandLineProcessor {

    // Same as the one defined in KCPDefaultGradlePlugin.kt
    override val pluginId: String = DEFAULT_COMPILER_PLUGIN_ID

    // Command line hints
    override val pluginOptions: Collection<AbstractCliOption>
        get() = KoncatArgument.values().map {
            CliOption(
                optionName = it.name,
                valueDescription = "string",
                description = it.desc,
                required = it.required
            )
        }

    // From cmd to compiler configuration
    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration
    ) {
        configuration.put(CompilerConfigurationKey(option.optionName), value)
    }

}