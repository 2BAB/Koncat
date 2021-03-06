import com.android.build.gradle.internal.tasks.factory.dependsOn

plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.devtools.ksp")
    id("me.2bab.koncat.android.app")
}

android {
    compileSdk = 31
    defaultConfig {
        applicationId = "me.xx2bab.seal.sample"
        minSdk = 23
        targetSdk = 31
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
        }
        getByName("release") {
            isMinifyEnabled = true
        }
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
    implementation(projects.androidLib)
    implementation(projects.kotlinLib)
    implementation(projects.symbols)
    implementation("me.2bab:koncat-sample-lib2:1.0")

    implementation(deps.kotlin.std)
    implementation(deps.koin.android)

    implementation(deps.koncat.runtime)
    ksp(deps.koncat.processor)
    ksp(projects.customProcessor)
    ksp(projects.dataProviderProcessor)
}

koncat {
    declaredAsMainProject.set(true)
    generateAggregationClass.set(true)
    generateExtensionClass.set(true)
    annotations.addAll("me.xx2bab.koncat.sample.annotation.ExportActivity")
    classTypes.addAll("me.xx2bab.koncat.sample.interfaze.DummyAPI")
    propertyTypes.addAll("org.koin.core.module.Module")
}

ksp {
    arg("option111", "value111")
}

afterEvaluate {
    ksp {
        val a = true
        arg("option222", a.toString())
    }
}


class InOutFilesProvider(
    @org.gradle.api.tasks.InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    val localProperties: File,
) : CommandLineArgumentProvider {
    override fun asArguments(): Iterable<String> {
        return listOf("customProc.localProperties=${localProperties.path}")
    }
}

val argTask by tasks.registering {
    doLast {
        ksp {
            arg("option333", "value333")
            arg(InOutFilesProvider(rootProject.layout
                        .projectDirectory
                        .file("local.properties")
                        .asFile
                ))
        }
    }
}

afterEvaluate {
    tasks.named("assembleDebug").dependsOn(argTask)
}