package me.xx2bab.koncat.compiler

import com.google.auto.service.AutoService
import me.xx2bab.koncat.contract.DEFAULT_COMPILER_PLUGIN_ID
import me.xx2bab.koncat.contract.KONCAT_PROCESSOR_ARGUMENT_KEY
import me.xx2bab.koncat.contract.KoncatArgument
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

@AutoService(CommandLineProcessor::class)
class KoncatCommentLineProcessor : CommandLineProcessor {

    companion object {

        internal val uniqueKeys = mutableMapOf<String, CompilerConfigurationKey<String>>()

        init {
            KoncatArgument.values().forEach { arg ->
                uniqueKeys[KONCAT_PROCESSOR_ARGUMENT_KEY + arg.name] = CompilerConfigurationKey(KONCAT_PROCESSOR_ARGUMENT_KEY + arg.name)
            }
        }

    }

    // Same as the one defined in KCPDefaultGradlePlugin.kt from koncat-gradle-plugin
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
        if (option.optionName.startsWith(KONCAT_PROCESSOR_ARGUMENT_KEY)) {
            configuration.put(
                uniqueKeys[option.optionName]!!,
                value
            )
        }
    }

}