package me.xx2bab.koncat.sample.kotlin

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import java.util.concurrent.atomic.AtomicBoolean

class DataProviderProcessorProvider : SymbolProcessorProvider {
    override fun create(
        environment: SymbolProcessorEnvironment
    ): SymbolProcessor {
        return DataProviderProcessor(
            environment.codeGenerator,
            environment.logger
        )
    }
}

class DataProviderProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {

    private val generated = AtomicBoolean(false)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (!generated.get()) {
            generated.set(true)
            val os = codeGenerator.createNewFile(
                Dependencies(aggregating = false),
                "me.xx2bab.koncat.sample",
                "DataProviderProcessorAPI",
                "kt"
            )
            os.write(
                """
                package me.xx2bab.koncat.sample
                
                import me.xx2bab.koncat.sample.interfaze.DummyAPI

                class DataProviderProcessorAPI : DummyAPI {

                    override fun onCall(param: String): String {
                        return "DataProviderProcessorAPI is running ..."
                    }

                }
                """.trimIndent().toByteArray()
            )
            os.close()
        }
        return emptyList()
    }

}
