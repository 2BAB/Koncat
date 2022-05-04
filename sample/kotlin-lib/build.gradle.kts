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
    implementation(deps.koin.core)

    implementation(deps.koncat.runtime)
    ksp(deps.koncat.processor)
    ksp(projects.customProcessor)
}

koncat {
    annotations.addAll("me.xx2bab.koncat.sample.annotation.ExportActivity")
    interfaces.addAll("me.xx2bab.koncat.sample.interfaze.DummyAPI")
    properties.addAll("org.koin.core.module.Module")
}