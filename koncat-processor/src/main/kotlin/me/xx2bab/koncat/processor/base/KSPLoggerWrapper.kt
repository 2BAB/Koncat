package me.xx2bab.koncat.processor.base

import com.google.devtools.ksp.processing.KSPLogger
import me.xx2bab.koncat.contract.KLogger
import me.xx2bab.koncat.contract.LOG_TAG

class KSPLoggerWrapper(val kspLogger: KSPLogger) : KLogger {
    override fun logging(message: String) {
        kspLogger.logging(LOG_TAG + message)
    }

    override fun info(message: String) {
        kspLogger.info(LOG_TAG + message)
    }

    override fun warn(message: String) {
        kspLogger.warn(LOG_TAG + message)
    }

    override fun error(message: String) {
        kspLogger.error(LOG_TAG + message)
    }
}