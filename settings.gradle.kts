rootProject.name = "koncat-root"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    val versions = file("deps.versions.toml").readText()
    val regexPlaceHolder = "%s\\s\\=\\s\\\"([A-Za-z0-9\\.\\-]+)\\\""
    val getVersion =
        { s: String -> regexPlaceHolder.format(s).toRegex().find(versions)!!.groupValues[1] }

    plugins {
        kotlin("jvm") version getVersion("kotlinVer") apply false
        kotlin("plugin.serialization") version getVersion("kotlinVer") apply false
        id("com.github.gmazzo.buildconfig") version getVersion("buildConfigVer") apply false
    }
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
        mavenLocal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
    versionCatalogs {
        create("deps") {
            from(files("./deps.versions.toml"))
        }
    }
}

include(
    // Compile-Time components
    ":koncat-compile-contract",
    ":koncat-gradle-plugin",
    ":koncat-processor",
    ":koncat-processor-api",

    // Runtime components
    ":koncat-runtime",
    ":koncat-runtime-model",
    ":koncat-runtime-stub", // A stub for runtime library to be used when released, later the generated class will replace it

    // Functional test for all components including the sample
    "functional-test"
)