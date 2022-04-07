package me.xx2bab.koncat.compiler

import me.xx2bab.koncat.contract.KLogger
import me.xx2bab.koncat.contract.KoncatArgument
import me.xx2bab.koncat.contract.KoncatArgumentsContract
import me.xx2bab.koncat.contract.LOG_TAG
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

class KoncatComponentRegistrar : ComponentRegistrar {

    override fun registerProjectComponents(
        project: MockProject,
        configuration: CompilerConfiguration
    ) {
//        val messageCollector = configuration.get(
//            CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY,
//            MessageCollector.NONE
//        )
        val map = mutableMapOf<String, String>()
        KoncatArgument.values().forEach {
            map[it.name] = configuration.get(CompilerConfigurationKey(it.name))!!
        }
        IrGenerationExtension.registerExtension(
            project,
            KoncatGenerationExtension(KoncatArgumentsContract(map, kLogger))
        )
    }

    //    val logger = Logger.getInstance(MockProject::class.java)
    val kLogger = object : KLogger {
        override fun logging(message: String) {
            println(LOG_TAG + message)
        }

        override fun info(message: String) {
            println(LOG_TAG + message)
        }

        override fun warn(message: String) {
            println(LOG_TAG + message)
        }

        override fun error(message: String) {
            println(LOG_TAG + message)
        }

    }
}