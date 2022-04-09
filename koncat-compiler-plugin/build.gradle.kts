plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
    `maven-central-publish`
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation(projects.koncatContract)
    implementation(deps.kotlin.std)
    compileOnly(deps.kotlin.compiler.embeddable)
    implementation("com.google.auto.service:auto-service-annotations:1.0.1")
    ksp("dev.zacsweers.autoservice:auto-service-ksp:1.0.0")

}