plugins {
    `kotlin-dsl`
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation(gradleApi())
    implementation(deps.kotlin.std)
}