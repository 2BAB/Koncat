package me.xx2bab.koncat.processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import me.xx2bab.koncat.api.KoncatProcAPI
import me.xx2bab.koncat.api.KoncatProcAPIImpl
import me.xx2bab.koncat.api.adapter.KSPAdapter
import me.xx2bab.koncat.processor.base.KSPLoggerWrapper

class KoncatProcessorProvider : SymbolProcessorProvider {
    override fun create(
        env: SymbolProcessorEnvironment
    ): SymbolProcessor {
        val koncat: KoncatProcAPI = KoncatProcAPIImpl(KSPAdapter(env))
        return if (koncat.isMainProject()) {
            KoncatAggregationProcessor(
                env.codeGenerator,
                KSPLoggerWrapper(env.logger),
                koncat
            )
        } else {
            KoncatMetaDataProcessor(
                env.codeGenerator,
                KSPLoggerWrapper(env.logger),
                koncat
            )
        }
    }
}
