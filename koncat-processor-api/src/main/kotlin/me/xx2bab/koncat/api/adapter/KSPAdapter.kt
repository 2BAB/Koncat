package me.xx2bab.koncat.api.adapter

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import me.xx2bab.koncat.api.AnnotationProcessorAdapter
import me.xx2bab.koncat.api.tool.VariantAwareness
import me.xx2bab.koncat.contract.KLogger
import me.xx2bab.koncat.contract.KoncatArgumentsContract
import me.xx2bab.koncat.contract.LOG_TAG

class KSPAdapter(private val env: SymbolProcessorEnvironment) : AnnotationProcessorAdapter {

    private val variantAwareness: VariantAwareness = VariantAwareness(env)

    override val logger: KLogger
        get() = object : KLogger {
            override fun logging(message: String) {
                env.logger.logging(LOG_TAG + message)
            }

            override fun info(message: String) {
                env.logger.info(LOG_TAG + message)
            }

            override fun warn(message: String) {
                env.logger.warn(LOG_TAG + message)
            }

            override fun error(message: String) {
                env.logger.error(LOG_TAG + message)
            }
        }

    override val arguments: KoncatArgumentsContract = KoncatArgumentsContract(env.options, logger)

    override val variantName: String = variantAwareness.variantName

}