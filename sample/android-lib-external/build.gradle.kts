plugins {
    id("com.android.library")
    kotlin("android")
    id("com.google.devtools.ksp")
    id("me.2bab.koncat.android.lib")
    `maven-publish`
}

android {
    compileSdk = 31
    defaultConfig {
        minSdk = 23
        targetSdk = 31
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    sourceSets["main"].java.srcDir("src/main/kotlin")

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    implementation(projects.annotations)
    implementation(deps.kotlin.std)
    ksp(projects.processors)
}


//////////// To set up a local maven release for external library testing
val groupName = "me.2bab"
val projectName = "koncat-sample-lib2"

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = groupName
            artifactId = projectName
            version = "1.0"

            afterEvaluate {
                from(components["release"])
            }
        }
    }
    // Configure MavenLocal repository
    repositories {
        maven {
            name = "myMavenlocal"
            url = uri( "../localm2/repository")
        }
    }
}


