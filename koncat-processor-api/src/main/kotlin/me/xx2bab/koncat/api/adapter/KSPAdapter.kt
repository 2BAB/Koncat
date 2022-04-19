package me.xx2bab.koncat.api.adapter

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import me.xx2bab.koncat.api.ProcessorAdapter
import me.xx2bab.koncat.api.tool.VariantAwareness
import me.xx2bab.koncat.contract.*
import java.io.File

class KSPAdapter(private val env: SymbolProcessorEnvironment) : ProcessorAdapter {

    private val variantAwareness: VariantAwareness = VariantAwareness(env)
    private val koncatDir: File

    init {
        val koncatDirPath = env.options[KONCAT_ARGUMENT_INTERMEDIATES_DIR]
        check(!koncatDirPath.isNullOrBlank()) { DIRECTORY_PARSE_ERROR }
        koncatDir = File(koncatDirPath)
        check(koncatDir.exists() && koncatDir.isDirectory) {
            DIRECTORY_NOT_EXIST_ERROR.format(koncatDirPath)
        }
    }

    override val logger: KLogger
        get() = object : KLogger {
            override fun logging(message: String) {
                env.logger.logging(message)
            }

            override fun info(message: String) {
                env.logger.info(message)
            }

            override fun warn(message: String) {
                env.logger.warn(message)
            }

            override fun error(message: String) {
                env.logger.error(message)
            }
        }

    override val intermediateDir: File = koncatDir

    override val arguments: KoncatArgumentsContract = parseKoncatArguments(koncatDir)

    override val variantName: String = variantAwareness.variantName

}