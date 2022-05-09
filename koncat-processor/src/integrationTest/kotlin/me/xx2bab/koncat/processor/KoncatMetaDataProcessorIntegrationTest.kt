package me.xx2bab.koncat.processor

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.kspArgs
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import me.xx2bab.koncat.api.KoncatProcMetadata
import me.xx2bab.koncat.contract.KONCAT_ARGUMENT_INTERMEDIATES_DIR
import me.xx2bab.koncat.processor.fixture.sourcesForDummyLib
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import java.io.File

class KoncatMetaDataProcessorIntegrationTest {
    /*
    {
        "annotatedClasses": {
        "kotlin.Suppress": [
        {
            "name": "me.xx2bab.koncat.sample.AnnotatedClass",
            "annotations": [
            {
                "name": "kotlin.Suppress",
                "arguments": {
                "names": "[msg]"
            }
            }
            ]
        }
        ]
    },
        "typedClasses": {
        "java.lang.Runnable": [
        "DummyRunnable"
        ]
    },
        "typedProperties": {
        "java.util.Random": [
        "prop"
        ]
    }
    }
    */

    @Test
    fun `libaray project generates meta file with KoncatMetaDataProcessor successfully`() {

        val compilation = KotlinCompilation().apply {
            // Common
            inheritClassPath = true
            verbose = true

            // KSP
            sources = sourcesForDummyLib
            symbolProcessorProviders = listOf(KoncatProcessorProvider())
            kspArgs = mutableMapOf(
                KONCAT_ARGUMENT_INTERMEDIATES_DIR to
                        File("./src/integrationTest/resources/dummy-lib").absolutePath
            )
        }

        // Overall result
        val result = compilation.compile()
        assertThat(result.exitCode, `is`(KotlinCompilation.ExitCode.OK))

        // Generated file check
        val koncatMetaForDummyLib = compilation.kspSourcesDir
            .walk()
            .filter { !it.isDirectory }
            .first()
        assertThat(koncatMetaForDummyLib.name, `is`("KoncatMetaForDummylib.kt"))

        val fileContent = koncatMetaForDummyLib.readText()
        val metaInJsonText = Regex("(?<=\"\"\").+(?=\"\"\")").find(fileContent)!!.groupValues[0]
        println("[KoncatMetaDataProcessorIntegrationTest] metaInJsonText: $metaInJsonText")
        val metadata = Json.decodeFromString<KoncatProcMetadata>(metaInJsonText)

        assertThat(metadata.annotatedClasses.size, `is`(1))
        val suppressList = metadata.annotatedClasses["kotlin.Suppress"]!!
        assertThat(suppressList.size, `is`(1))
        assertThat(suppressList[0].name, `is`("me.xx2bab.koncat.sample.AnnotatedClass"))
        assertThat(suppressList[0].annotations[0].arguments.values.first(), `is`("[msg]"))

        assertThat(metadata.typedClasses.size, `is`(1))
        val runnableList = metadata.typedClasses["java.lang.Runnable"]!!
        assertThat(runnableList.size, `is`(1))
        assertThat(runnableList[0], `is`("me.xx2bab.koncat.sample.DummyRunnable"))

        assertThat(metadata.typedProperties.size, `is`(1))
        val randomList = metadata.typedProperties["java.util.Random"]!!
        assertThat(randomList.size, `is`(1))
        assertThat(randomList[0], `is`("me.xx2bab.koncat.sample.prop"))
    }

}