plugins {
    kotlin("jvm")
}

dependencies {
    implementation(projects.symbols)
    implementation(deps.kotlin.std)
    implementation(deps.kotlinpoet.core)
    implementation(deps.kotlinpoet.ksp)
    implementation(deps.ksp.api)
    implementation(deps.koncat.processor.api)
}
