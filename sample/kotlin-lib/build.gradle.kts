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
    implementation(projects.annotations)
    implementation(deps.kotlin.std)
    ksp(projects.processors)
}