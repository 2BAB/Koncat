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
    // Core components
    ":koncat-contract",
    ":koncat-gradle-plugin",
    ":koncat-processor-api",

    // Cupcake is one of the implementation based on Koncat aggregation mechanism,
    // it can aggregate:
    //     - Annotation classes
    //     - Interface Implementation classes
    //     - Typed Properties
    // for multi modules during compile time.
    ":koncat-cupcake-processor",
    ":koncat-cupcake-runtime",
    ":koncat-cupcake-model",
    ":koncat-cupcake-stub", // A stub for cupcake-runtime to be used when released, later the generated class will replace it

    // Functional test for all components including the sample
    "functional-test"
)