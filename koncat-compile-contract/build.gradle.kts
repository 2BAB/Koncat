plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    `maven-central-publish`
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation(deps.kotlin.std)
    compileOnly(deps.kotlin.serialization)
    compileOnly(deps.ksp.api)
}