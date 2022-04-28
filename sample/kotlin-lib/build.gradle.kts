plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
    id("me.2bab.koncat.jvm")
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation(projects.symbols)
    implementation(deps.kotlin.std)

    ksp(deps.koncat.cupcake.processor)

    ksp(projects.customProcessor)
}

koncat {
    defaultProcessor {
        annotations.addAll("me.xx2bab.koncat.sample.annotation.ExportActivity")
    }
}