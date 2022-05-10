package me.xx2bab.koncat

import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import me.xx2bab.koncat.contract.KoncatArgumentsContract
import org.gradle.testkit.runner.GradleRunner
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.`is`
import org.hamcrest.core.StringContains
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.lang.management.ManagementFactory

class SampleProjectTest {

    companion object {

        private const val baseTestProjectPath = "../sample"

        private const val aggregatedMetaData1OutputPathForKoncatProc =
            "%s/app/build/generated/ksp/debug/kotlin/me/xx2bab/koncat/runtime/meta/KoncatAggregatedMeta1.kt"
        private const val aggregatedMetaData2OutputPathForKoncatProc =
            "%s/app/build/generated/ksp/debug/kotlin/me/xx2bab/koncat/runtime/meta/KoncatAggregatedMeta2.kt"
        private const val aggregatedClassOutputPathForKoncatProc =
            "%s/app/build/generated/ksp/debug/kotlin/me/xx2bab/koncat/runtime/KoncatAggregation.kt"
        private const val aggregatedClassOutputPathForCustomProc =
            "%s/app/build/generated/ksp/debug/kotlin/me/xx2bab/koncat/sample/CustomRouterImpl.kt"
        private const val kotlinLibMetaOutputPath =
            "%s/kotlin-lib/build/generated/ksp/main/kotlin/me/xx2bab/koncat/runtime/meta/KoncatMetaForKotlinlib.kt"
        private const val androidLibMetaOutputPath =
            "%s/android-lib/build/generated/ksp/debug/kotlin/me/xx2bab/koncat/runtime/meta/KoncatMetaForAndroidlib.kt"
        private const val kotlinLibBaseArgumentsJSONFilePath =
            "%s/kotlin-lib/build/intermediates/koncat/base.json"

        private val allModules = arrayOf("android-lib", "android-lib-external", "kotlin-lib")


        @BeforeAll
        @JvmStatic
        fun setup() {
            // TODO: run each of them in parallel (will require using remote dependency)
            agpVerProvider().forEach { buildTestProject(it) }
        }

        private fun buildTestProject(agpVer: String) {
            println(
                "Copying project for AGP [${agpVer}] from ${
                    File(
                        baseTestProjectPath
                    ).absolutePath
                }..."
            )
            val targetProject = File("./build/sample-$agpVer")
            targetProject.deleteRecursively()
            File(baseTestProjectPath).copyRecursively(targetProject)
            val settings = File(targetProject, "settings.gradle.kts")
            val newSettings = settings.readText()
                .replace("= \"../\"", "= \"../../../\"") // Redirect the base dir
                .replace(
                    "getVersion(\"agpVer\")",
                    "\"$agpVer\""
                ) // Hardcode agp version
            settings.writeText(newSettings)

            println("assembleDebug for [$agpVer]")
            GradleRunner.create().apply {
                forwardOutput()
                withProjectDir(targetProject)
                withGradleVersion("7.4.1")
                withArguments("clean", "assembleDebug", "--stacktrace")
                // Ensure this value is true when `--debug-jvm` is passed to Gradle, and false otherwise
                withDebug(
                    ManagementFactory.getRuntimeMXBean().inputArguments.toString()
                        .indexOf("-agentlib:jdwp") > 0
                )
                build()
            }
            println("Built [${agpVer}] successfully.")
        }

        @JvmStatic
        fun agpVerProvider(): List<String> {
            val versions = File("../deps.versions.toml").readText()
            val regexPlaceHolder = "%s\\s\\=\\s\\\"([A-Za-z0-9\\.\\-]+)\\\""
            val getVersion =
                { s: String ->
                    regexPlaceHolder.format(s).toRegex().find(versions)!!.groupValues[1]
                }
            return listOf(getVersion("agpVer"), getVersion("agpBetaVer"))
        }
    }


    /*Koncat Processor cases*/

    @ParameterizedTest
    @MethodSource("agpVerProvider")
    fun `koncat base arguments JSON file is generated successfully`(agpVer: String) {
        val argsInText = File(kotlinLibBaseArgumentsJSONFilePath.format("./build/sample-$agpVer")).readText()
        val argsInJSON = Json.decodeFromString<KoncatArgumentsContract>(argsInText)
        assertThat(argsInJSON.projectName, `is`("kotlin-lib"))
        assertThat(argsInJSON.koncatVersion, Matchers.notNullValue())
        assertThat(argsInJSON.gradlePlugins, Matchers.notNullValue())
        assertThat(argsInJSON.declaredAsMainProject, `is`(false))
        assertThat(argsInJSON.generateAggregationClass, `is`(true))
        assertThat(argsInJSON.generateExtensionClass, `is`(false))
        assertThat(argsInJSON.targetAnnotations.first(), `is`("me.xx2bab.koncat.sample.annotation.ExportActivity"))
        assertThat(argsInJSON.targetClassTypes.first(), `is`("me.xx2bab.koncat.sample.interfaze.DummyAPI"))
        assertThat(argsInJSON.targetPropertyTypes.first(), `is`("org.koin.core.module.Module"))
    }

    @ParameterizedTest
    @MethodSource("agpVerProvider")
    fun `meta files are generated successfully for koncat proc`(agpVer: String) {
        // TODO: after we stabilized the JSON structure, can change to JSON object validation
        mapOf(
            kotlinLibMetaOutputPath to "\"me.xx2bab.koncat.sample.annotation.ExportActivity\":[]",
            androidLibMetaOutputPath to "me.xx2bab.koncat.sample.android.AndroidLibraryActivity",
        ).forEach { (filePath, targetText) ->
            val file = File(filePath.format("./build/sample-$agpVer"))
            assertThat(file.readText(), StringContains.containsString(targetText))
        }
    }

    @ParameterizedTest
    @MethodSource("agpVerProvider")
    fun `final class is generated successfully for koncat proc`(agpVer: String) {
        val genClass =
            File(aggregatedClassOutputPathForKoncatProc.format("./build/sample-$agpVer")).readText()
        listOf(
            "me.xx2bab.koncat.sample.MainActivity",
            "me.xx2bab.koncat.sample.annotation.ExportActivity",
            "me.xx2bab.koncat.sample.annotation.CustomMark",
            "me.xx2bab.koncat.sample.annotation.MemberRequired",
            "\"level\" to \"1\"",
            "me.xx2bab.koncat.sample.android.AndroidLibraryAPI",
            "me.xx2bab.koncat.sample.kotlin.kotlinLibraryModule"
        ).forEach {
            assertThat(genClass, StringContains.containsString(it))
        }
    }

    @ParameterizedTest
    @MethodSource("agpVerProvider")
    fun `aggregated meta files are generated successfully for koncat proc`(agpVer: String) {
        val gen1 = File(
            aggregatedMetaData1OutputPathForKoncatProc
                .format("./build/sample-$agpVer")
        ).readText()
        val gen2 = File(
            aggregatedMetaData2OutputPathForKoncatProc
                .format("./build/sample-$agpVer")
        ).readText()
        listOf(
            "me.xx2bab.koncat.sample.MainActivity",
            "me.xx2bab.koncat.sample.annotation.ExportActivity",
            "me.xx2bab.koncat.sample.annotation.CustomMark",
            "me.xx2bab.koncat.sample.annotation.MemberRequired",
            "me.xx2bab.koncat.sample.android.AndroidLibraryAPI",
            "me.xx2bab.koncat.sample.kotlin.kotlinLibraryModule"
        ).forEach {
            assertThat(gen1, StringContains.containsString(it))
        }
        assertThat(gen2, StringContains.containsString("me.xx2bab.koncat.sample.DataProviderProcessorAPI"))
    }



    /*Extend Processor cases*/

    @ParameterizedTest
    @MethodSource("agpVerProvider")
    fun `extension router class is generated successfully for custom proc`(agpVer: String) {
        val genClass =
            File(aggregatedClassOutputPathForCustomProc.format("./build/sample-$agpVer")).readText()
        listOf(
            "me.xx2bab.koncat.sample.android.ExternalAndroidLibraryAPI",
            "me.xx2bab.koncat.sample.android.AndroidLibraryAPI",
            "me.xx2bab.koncat.sample.kotlin.KotlinLibraryAPI",
            "me.xx2bab.koncat.sample.DataProviderProcessorAPI"
        ).forEach {
            assertThat(genClass, StringContains.containsString(it))
        }
    }

}