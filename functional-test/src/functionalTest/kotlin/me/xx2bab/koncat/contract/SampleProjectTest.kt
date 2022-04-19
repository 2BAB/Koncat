package me.xx2bab.koncat.contract

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.lang.management.ManagementFactory

class SampleProjectTest {

    companion object {

        private const val baseTestProjectPath = "../sample"
        private const val aggregatedClassOutputPath =
            "%s/app/build/generated/ksp/debug/kotlin/me/xx2bab/koncat/sample/ExportCapabilityRouterImpl.kt"
        private const val aggregatedJsonOutputPath = "%s/app/build/intermediates/koncat/debug"
        private const val kotlinLibJsonOutputPath =
            "%s/kotlin-lib/build/generated/ksp/main/resources/kotlin-lib-export.json.koncat"
        private const val androidLibJsonOutputPath =
            "%s/android-lib/build/generated/ksp/debug/resources/android-lib-export.json.koncat"

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


    @ParameterizedTest
    @MethodSource("agpVerProvider")
    fun koncatFilesAreGeneratedSuccessfully(agpVer: String) {
        val kotlinLibGenFile = File(kotlinLibJsonOutputPath.format("./build/sample-$agpVer"))
        assertTrue(
            kotlinLibGenFile.readText()
                .contains("me.xx2bab.koncat.sample.kotlin.ExportedPureKotlinLibraryRunnable")
        )
        val androidLibGenFile = File(androidLibJsonOutputPath.format("./build/sample-$agpVer"))
        assertTrue(
            androidLibGenFile.readText()
                .contains("me.xx2bab.koncat.sample.android.ExportedAndroidLibraryRunnable")
        )
    }

    @ParameterizedTest
    @MethodSource("agpVerProvider")
    fun koncatFilesAreExtractedSuccessfully(agpVer: String) {
        val targetDir = File(aggregatedJsonOutputPath.format("./build/sample-$agpVer"))
        assertTrue(targetDir.list().size == 3)
        assertTrue(targetDir.list().contains("android-lib-export.json.koncat"))
        assertTrue(targetDir.list().contains("android-lib-external-export.json.koncat"))
        assertTrue(targetDir.list().contains("kotlin-lib-export.json.koncat"))
    }

    @ParameterizedTest
    @MethodSource("agpVerProvider")
    fun finalClassIsGeneratedSuccessfully(agpVer: String) {
        val targetFile = File(aggregatedClassOutputPath.format("./build/sample-$agpVer"))
        assertTrue(
            targetFile.readText().contains(
                "fun getExportAPIList()"
            )
        )
        assertTrue(
            targetFile.readText().contains(
                "me.xx2bab.koncat.sample.android.ExportedAndroidLibraryExternalRunnable"
            )
        )
        assertTrue(
            targetFile.readText().contains(
                "me.xx2bab.koncat.sample.kotlin.ExportedPureKotlinLibraryRunnable"
            )
        )
        assertTrue(
            targetFile.readText().contains(
                "me.xx2bab.koncat.sample.android.ExportedAndroidLibraryRunnable"
            )
        )
    }

}