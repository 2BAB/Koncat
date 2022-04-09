package me.xx2bab.koncat.compiler

import com.google.auto.service.AutoService
import me.xx2bab.koncat.compiler.KoncatCommentLineProcessor.Companion.uniqueKeys
import me.xx2bab.koncat.contract.KLogger
import me.xx2bab.koncat.contract.KONCAT_PROCESSOR_ARGUMENT_KEY
import me.xx2bab.koncat.contract.KoncatArgument
import me.xx2bab.koncat.contract.KoncatArgumentsContract
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration

@AutoService(ComponentRegistrar::class)
class KoncatComponentRegistrar : ComponentRegistrar {

    override fun registerProjectComponents(
        project: MockProject,
        configuration: CompilerConfiguration
    ) {
        val messageCollector = configuration.get(
            CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY,
            MessageCollector.NONE
        )
        messageCollector.report(CompilerMessageSeverity.WARNING, "[Koncat] registerProjectComponents")
        val kLogger = getKLogger(messageCollector)
        val map = mutableMapOf<String, String>()
        KoncatArgument.values().forEach {
            val key = KONCAT_PROCESSOR_ARGUMENT_KEY + it.name
            map[key] = configuration.get(uniqueKeys[key]!!)!!
        }
        val koncat = KoncatArgumentsContract(map, kLogger)
        IrGenerationExtension.registerExtension(
            project,
            KoncatGenerationExtension(koncat, kLogger)
        )
    }

    private fun getKLogger(messageCollector: MessageCollector) = object : KLogger {
        override fun logging(message: String) {
            messageCollector.report(CompilerMessageSeverity.LOGGING, message)
        }

        override fun info(message: String) {
            messageCollector.report(CompilerMessageSeverity.INFO, message)
        }

        override fun warn(message: String) {
            messageCollector.report(CompilerMessageSeverity.WARNING, message)
        }

        override fun error(message: String) {
            messageCollector.report(CompilerMessageSeverity.ERROR, message)
        }

    }
}