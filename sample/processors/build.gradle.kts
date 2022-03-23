plugins {
    kotlin("jvm")
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation(projects.annotations)
    implementation(deps.kotlin.std)
    implementation(deps.ksp)
}