package me.xx2bab.koncat.contract

import org.gradle.testkit.runner.GradleRunner
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasItemInArray
import org.hamcrest.core.StringContains
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.lang.management.ManagementFactory

class SampleProjectTest {

    companion object {

        private const val baseTestProjectPath = "../sample"

        private const val customProcId = "-custom-mark-proc"
        private const val cupcakeProcId = "-cupcake-proc"

        private const val aggregatedClassOutputPathForCupcakeProc =
            "%s/app/build/generated/ksp/debug/kotlin/me/xx2bab/koncat/cupcake/KoncatCupCakeAggregation.kt"
        private const val aggregatedClassOutputPathForCustomProc =
            "%s/app/build/generated/ksp/debug/kotlin/me/xx2bab/koncat/sample/CustomRouterImpl.kt"
        private const val aggregatedJsonOutputPath = "%s/app/build/intermediates/koncat/debug"
        private const val kotlinLibJsonOutputPath =
            "%s/kotlin-lib/build/generated/ksp/main/resources/kotlin-lib%s.json.koncat"
        private const val androidLibJsonOutputPath =
            "%s/android-lib/build/generated/ksp/debug/resources/android-lib%s.json.koncat"

        private val allModules = arrayOf("android-lib", "android-lib-external", "kotlin-lib")
        private const val jsonKoncat = ".json.koncat"


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



    /*Cupcake Processor cases*/

    @ParameterizedTest
    @MethodSource("agpVerProvider")
    fun koncatFilesAreGeneratedSuccessfullyForCupcakeProc(agpVer: String) {
        // TODO: after we stabilized the JSON structure, can change to JSON object validation
        mapOf(
            kotlinLibJsonOutputPath to "\"me.xx2bab.koncat.sample.annotation.ExportActivity\":[]",
            androidLibJsonOutputPath to "me.xx2bab.koncat.sample.android.AndroidLibraryActivity",
        ).forEach { (filePath, targetText) ->
            val file = File(filePath.format("./build/sample-$agpVer", cupcakeProcId))
            assertThat(file.readText(), StringContains.containsString(targetText))
        }
    }

    @ParameterizedTest
    @MethodSource("agpVerProvider")
    fun koncatFilesAreExtractedSuccessfullyForCupcakeProc(agpVer: String) {
        val targetFileNames = File(aggregatedJsonOutputPath.format("./build/sample-$agpVer")).list()
        allModules.forEach {
            assertThat(targetFileNames, hasItemInArray(it + cupcakeProcId + jsonKoncat))
        }
    }

    @ParameterizedTest
    @MethodSource("agpVerProvider")
    fun finalClassIsGeneratedSuccessfullyForCupcakeProc(agpVer: String) {
        val genClass = File(aggregatedClassOutputPathForCupcakeProc.format("./build/sample-$agpVer")).readText()
        listOf(
            "me.xx2bab.koncat.sample.MainActivity",
            "me.xx2bab.koncat.sample.annotation.ExportActivity",
            "me.xx2bab.koncat.sample.annotation.CustomMark",
            "me.xx2bab.koncat.sample.annotation.MemberRequired",
            "\"level\" to \"1\""
        ).forEach {
            assertThat(genClass, StringContains.containsString(it))
        }
    }



    /*Custom Processor cases*/

    @ParameterizedTest
    @MethodSource("agpVerProvider")
    fun koncatFilesAreGeneratedSuccessfullyForCustomProc(agpVer: String) {
        mapOf(
            androidLibJsonOutputPath to "me.xx2bab.koncat.sample.android.AndroidLibraryActivity",
        ).forEach { (filePath, targetText) ->
            val file = File(filePath.format("./build/sample-$agpVer", customProcId))
            assertThat(file.readText(), StringContains.containsString(targetText))
        }
    }

    @ParameterizedTest
    @MethodSource("agpVerProvider")
    fun koncatFilesAreExtractedSuccessfullyForCustomProc(agpVer: String) {
        val targetFileNames = File(aggregatedJsonOutputPath.format("./build/sample-$agpVer")).list()
        allModules.forEach {
            assertThat(targetFileNames, hasItemInArray(it + customProcId + jsonKoncat))
        }
    }

    @ParameterizedTest
    @MethodSource("agpVerProvider")
    fun finalClassIsGeneratedSuccessfullyForCustomProc(agpVer: String) {
        val genClass = File(aggregatedClassOutputPathForCustomProc.format("./build/sample-$agpVer")).readText()
        listOf(
            "me.xx2bab.koncat.sample.MainActivity",
            "me.xx2bab.koncat.sample.MainActivity.NestedClass",
            "me.xx2bab.koncat.sample.MainActivity.InnerClass",
            "me.xx2bab.koncat.sample.android.AndroidLibraryActivity"
        ).forEach {
            assertThat(genClass, StringContains.containsString(it))
        }
    }

}