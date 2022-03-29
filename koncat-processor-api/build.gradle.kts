plugins {
    kotlin("jvm")
}

group = "me.2bab"

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    api(projects.koncatContract)
    implementation(deps.kotlin.std)
    implementation(deps.kotlin.reflect)
    compileOnly(deps.ksp.api)
    compileOnly(deps.ksp.impl)
}