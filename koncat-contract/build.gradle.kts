plugins {
    kotlin("jvm")
    `maven-central-publish`
}

group = "me.2bab"

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation(deps.kotlin.std)
    compileOnly(deps.ksp.api)
}