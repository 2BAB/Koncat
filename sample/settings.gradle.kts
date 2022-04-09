rootProject.name = "koncat-sample"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

val externalDependencyBaseDir = extra["externalDependencyBaseDir"].toString()

pluginManagement {
    extra["externalDependencyBaseDir"] = "../"
    val versions = file(extra["externalDependencyBaseDir"].toString() + "deps.versions.toml").readText()
    val regexPlaceHolder = "%s\\s\\=\\s\\\"([A-Za-z0-9\\.\\-]+)\\\""
    val getVersion = { s: String -> regexPlaceHolder.format(s).toRegex().find(versions)!!.groupValues[1] }

    plugins {
        kotlin("android") version getVersion("kotlinVer") apply false
        kotlin("jvm") version getVersion("kotlinVer") apply false
        kotlin("plugin.serialization") version getVersion("kotlinVer") apply false
        id("com.android.application") version getVersion("agpVer") apply false
        id("com.android.library") version getVersion("agpVer") apply false
        id("com.google.devtools.ksp") version getVersion("kspVer") apply false
    }
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        gradlePluginPortal()
    }

    resolutionStrategy {
        eachPlugin {
            if(requested.id.id.startsWith("me.2bab.koncat")) {
                // It will be replaced by a local module using `includeBuild` below,
                // thus we just put a generic version (+) here.
                useModule("me.2bab:koncat-gradle-plugin:+")
            }
        }
    }
}

dependencyResolutionManagement {
    repositories {
        maven {
            repositories {
                setUrl("./localm2/repository")
            }
        }
        google()
        mavenCentral()
        mavenLocal()
    }
    versionCatalogs {
        create("deps") {
            from(files(externalDependencyBaseDir + "deps.versions.toml"))
        }
    }
}

include(":app",
    ":android-lib",
    ":android-lib-external",
    ":kotlin-lib",
    ":annotations",
    ":processors")

includeBuild(externalDependencyBaseDir){
    dependencySubstitution {
        substitute(module("me.2bab:koncat-gradle-plugin"))
            .using(project(":koncat-gradle-plugin"))
    }
    dependencySubstitution {
        substitute(module("me.2bab:koncat-processor-api"))
            .using(project(":koncat-processor-api"))
    }
    dependencySubstitution {
        substitute(module("me.2bab:koncat-compiler-plugin"))
            .using(project(":koncat-compiler-plugin"))
    }
}