package me.xx2bab.koncat.processor.fixture

import com.tschuchort.compiletesting.SourceFile

val sourcesForDummyLib = listOf(
    SourceFile.kotlin(
        "AnnotatedClass.kt", """
                package me.xx2bab.koncat.sample
                @Suppress("msg")
                class AnnotatedClass
            """.trimIndent()
    ),
    SourceFile.kotlin(
        "DummyRunnable.kt", """
                package me.xx2bab.koncat.sample
                class DummyRunnable: Runnable {
                    override fun run() {}
                }
            """.trimIndent()
    ),
    SourceFile.kotlin(
        "DummyPropFile.kt", """
                package me.xx2bab.koncat.sample
                val prop = java.util.Random()
            """.trimIndent()
    )
)

val generatedFileOfDummyLib =
    SourceFile.kotlin(
        "DummyPropFile.kt", """
                package me.xx2bab.koncat.runtime.meta

                import me.xx2bab.koncat.runtime.KoncatMeta
                
                @KoncatMeta(metaDataInJson = ""${'"'}{"annotatedClasses":{"kotlin.Suppress":[{"name":"me.xx2bab.koncat.sample.AnnotatedClass","annotations":[{"name":"kotlin.Suppress","arguments":{"names":"[msg]"}}]}]},"typedClasses":{"java.lang.Runnable":["me.xx2bab.koncat.sample.DummyRunnable"]},"typedProperties":{"java.util.Random":["me.xx2bab.koncat.sample.prop"]}}""${'"'})
                val voidProp = null // DO NOT use voidProp directly, the valuable information is placing in `metaDataInJson` above.
            """.trimIndent()
    )

val sourcesForDummyApp = listOf(
    SourceFile.kotlin(
        "AnnotatedClass2.kt", """
                package me.xx2bab.koncat.sample
                @Suppress("msg")
                class AnnotatedClass2
            """.trimIndent()
    ),
    SourceFile.kotlin(
        "DummyRunnable2.kt", """
                package me.xx2bab.koncat.sample
                class DummyRunnable2: Runnable {
                    override fun run() {}
                }
            """.trimIndent()
    ),
    SourceFile.kotlin(
        "DummyPropFile2.kt", """
                package me.xx2bab.koncat.sample
                val prop2 = java.util.Random()
            """.trimIndent()
    )
)