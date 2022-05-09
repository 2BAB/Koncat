package me.xx2bab.koncat.processor

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.kspArgs
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import me.xx2bab.koncat.api.KoncatProcMetadata
import me.xx2bab.koncat.contract.KONCAT_ARGUMENT_INTERMEDIATES_DIR
import me.xx2bab.koncat.processor.fixture.generatedFileOfDummyLib
import me.xx2bab.koncat.processor.fixture.sourcesForDummyApp
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.core.StringContains
import org.junit.jupiter.api.Test
import java.io.File

class KoncatAggregationProcessorIntegrationTest {

    @Test
    fun `app project generates extension meta file with KoncatAggregationProcessor successfully`() {
        val compilation = KotlinCompilation().apply {
            // Common
            inheritClassPath = true
            verbose = true

            // KSP
            sources = sourcesForDummyApp + generatedFileOfDummyLib

            symbolProcessorProviders = listOf(KoncatProcessorProvider())
            kspArgs = mutableMapOf(
                KONCAT_ARGUMENT_INTERMEDIATES_DIR to
                        File("./src/integrationTest/resources/dummy-app/extension/").absolutePath
            )
        }

        // Overall result
        val result = compilation.compile()
        assertThat(result.exitCode, Matchers.`is`(KotlinCompilation.ExitCode.OK))

        // Generated file check
        val koncatMetaForDummyApp = compilation.kspSourcesDir
            .walk()
            .filter { !it.isDirectory }
            .first()
        assertThat(
            koncatMetaForDummyApp.name,
            Matchers.`is`("KoncatAggregatedMeta1.kt")
        )

        val fileContent = koncatMetaForDummyApp.readText()
        val metaInJsonText = Regex("(?<=\"\"\").+(?=\"\"\")").find(fileContent)!!.groupValues[0]
        println("[KoncatAggregationProcessorIntegrationTest] extension: $metaInJsonText")
        val metadata = Json.decodeFromString<KoncatProcMetadata>(metaInJsonText)

        assertThat(metadata.annotatedClasses.size, Matchers.`is`(1))
        val suppressList = metadata.annotatedClasses["kotlin.Suppress"]!!
        assertThat(suppressList.size, Matchers.`is`(2))
        assertThat(
            suppressList[0].name,
            Matchers.`is`("me.xx2bab.koncat.sample.AnnotatedClass")
        )
        assertThat(
            suppressList[1].name,
            Matchers.`is`("me.xx2bab.koncat.sample.AnnotatedClass2")
        )
        assertThat(
            suppressList[0].annotations[0].arguments.values.first(),
            Matchers.`is`("[msg]")
        )

        assertThat(metadata.typedClasses.size, Matchers.`is`(1))
        val runnableList = metadata.typedClasses["java.lang.Runnable"]!!
        assertThat(runnableList.size, Matchers.`is`(2))
        assertThat(
            runnableList[0],
            Matchers.`is`("me.xx2bab.koncat.sample.DummyRunnable")
        )
        assertThat(
            runnableList[1],
            Matchers.`is`("me.xx2bab.koncat.sample.DummyRunnable2")
        )

        assertThat(metadata.typedProperties.size, Matchers.`is`(1))
        val randomList = metadata.typedProperties["java.util.Random"]!!
        assertThat(randomList.size, Matchers.`is`(2))
        assertThat(randomList[0], Matchers.`is`("me.xx2bab.koncat.sample.prop"))
        assertThat(randomList[1], Matchers.`is`("me.xx2bab.koncat.sample.prop2"))
    }


    @Test
    fun `app project generates aggregated router file with KoncatAggregationProcessor successfully`() {
        val compilation = KotlinCompilation().apply {
            // Common
            inheritClassPath = true
            verbose = true

            // KSP
            sources = sourcesForDummyApp + generatedFileOfDummyLib
            symbolProcessorProviders = listOf(KoncatProcessorProvider())
            kspArgs = mutableMapOf(
                KONCAT_ARGUMENT_INTERMEDIATES_DIR to
                        File("./src/integrationTest/resources/dummy-app/aggregation/").absolutePath
            )
        }

        // Overall result
        val result = compilation.compile()
        assertThat(result.exitCode, Matchers.`is`(KotlinCompilation.ExitCode.OK))

        // Generated file check
        val koncatAggregationRouterForDummyApp = compilation.kspSourcesDir
            .walk()
            .filter { !it.isDirectory }
            .first()
        assertThat(
            koncatAggregationRouterForDummyApp.name,
            Matchers.`is`("KoncatAggregation.kt")
        )

        val fileContent = koncatAggregationRouterForDummyApp.readText()
        println("[KoncatAggregationProcessorIntegrationTest] aggregation:$fileContent")
        listOf(
            "me.xx2bab.koncat.sample.AnnotatedClass",
            "me.xx2bab.koncat.sample.AnnotatedClass2",
            "me.xx2bab.koncat.sample.DummyRunnable",
            "me.xx2bab.koncat.sample.DummyRunnable2",
            "me.xx2bab.koncat.sample.prop", "me.xx2bab.koncat.sample.prop2"
        ).forEach {
            assertThat(fileContent, StringContains.containsString(it))
        }
    }

}