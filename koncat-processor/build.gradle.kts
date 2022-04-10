plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation(deps.kotlin.std)
    implementation(deps.kotlin.serialization)
    implementation(deps.kotlinpoet.core)
    implementation(deps.kotlinpoet.ksp)
    implementation(deps.ksp.api)
    implementation(deps.koncat.processor.api)
}