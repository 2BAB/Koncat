plugins {
    id("com.android.library")
    kotlin("android")
    id("com.google.devtools.ksp")
    id("me.2bab.koncat.android.lib")
}

android {
    compileSdk = 31
    defaultConfig {
        minSdk = 23
        targetSdk = 31
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    lint {
//        isAbortOnError = false
    }

    sourceSets["main"].java.srcDir("src/main/kotlin")

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(projects.symbols)
    implementation(deps.kotlin.std)
    implementation(deps.koin.android)

    implementation(deps.koncat.runtime)
    ksp(deps.koncat.processor)
}

koncat {
    annotations.addAll("me.xx2bab.koncat.sample.annotation.ExportActivity")
    classTypes.addAll("me.xx2bab.koncat.sample.interfaze.DummyAPI")
    propertyTypes.addAll("org.koin.core.module.Module")
}