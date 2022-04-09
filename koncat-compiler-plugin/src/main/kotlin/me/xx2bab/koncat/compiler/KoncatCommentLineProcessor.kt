package me.xx2bab.koncat.compiler

import com.google.auto.service.AutoService
import me.xx2bab.koncat.contract.DEFAULT_COMPILER_PLUGIN_ID
import me.xx2bab.koncat.contract.KoncatArgument
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

@AutoService(CommandLineProcessor::class)
class KoncatCommentLineProcessor : CommandLineProcessor {

    // Same as the one defined in KCPDefaultGradlePlugin.kt
    override val pluginId: String = DEFAULT_COMPILER_PLUGIN_ID

    // Command line hints
    override val pluginOptions: Collection<AbstractCliOption> = KoncatArgument.values().map {
        CliOption(
            optionName = KONCAT_PROCESSOR_ARGUMENT_KEY + it.name,
            valueDescription = "String",
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