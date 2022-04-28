plugins {
    kotlin("jvm")
    `maven-central-publish`
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    compileOnly(projects.koncatCupcakeModel)
}