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
import org.hamcrest.Matchers.`is`
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
        assertThat(result.exitCode, `is`(KotlinCompilation.ExitCode.OK))

        // Generated file check
        val koncatMetaForDummyAppList = compilation.kspSourcesDir
            .walk()
            .filter { !it.isDirectory }
            .toList()
        assertThat(
            koncatMetaForDummyAppList.size,
            `is`(2)
        )

        val fileContent = koncatMetaForDummyAppList[0].readText()
//        val metaInJsonText = Regex("(?<=\"\"\").+(?=\"\"\")").find(fileContent)!!.groupValues[0]
        println("[KoncatAggregationProcessorIntegrationTest] extension: $fileContent")
        val metadata = Json.decodeFromString<KoncatProcMetadata>(fileContent)

        assertThat(metadata.annotatedClasses.size, `is`(1))
        val suppressList = metadata.annotatedClasses["kotlin.Suppress"]!!
        assertThat(suppressList.size, `is`(2))
        assertThat(
            suppressList[0].name,
            `is`("me.xx2bab.koncat.sample.AnnotatedClass")
        )
        assertThat(
            suppressList[1].name,
            `is`("me.xx2bab.koncat.sample.AnnotatedClass2")
        )
        assertThat(
            suppressList[0].annotations[0].arguments.values.first(),
            `is`("[msg]")
        )

        assertThat(metadata.typedClasses.size, `is`(1))
        val runnableList = metadata.typedClasses["java.lang.Runnable"]!!
        assertThat(runnableList.size, `is`(2))
        assertThat(
            runnableList[0],
            `is`("me.xx2bab.koncat.sample.DummyRunnable")
        )
        assertThat(
            runnableList[1],
            `is`("me.xx2bab.koncat.sample.DummyRunnable2")
        )

        assertThat(metadata.typedProperties.size, `is`(1))
        val randomList = metadata.typedProperties["java.util.Random"]!!
        assertThat(randomList.size, `is`(2))
        assertThat(randomList[0], `is`("me.xx2bab.koncat.sample.prop"))
        assertThat(randomList[1], `is`("me.xx2bab.koncat.sample.prop2"))
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
        assertThat(result.exitCode, `is`(KotlinCompilation.ExitCode.OK))

        // Generated file check
        val koncatAggregationRouterForDummyApp = compilation.kspSourcesDir
            .walk()
            .filter { !it.isDirectory }
            .first()
        assertThat(
            koncatAggregationRouterForDummyApp.name,
            `is`("KoncatAggregation.kt")
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