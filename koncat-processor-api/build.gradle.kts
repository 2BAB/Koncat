plugins {
    kotlin("jvm")
    id("com.github.gmazzo.buildconfig")
    kotlin("plugin.serialization")
    `maven-central-publish`
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    api(projects.koncatCompileContract)
    implementation(projects.koncatRuntimeModel)
    implementation(deps.kotlin.std)
    implementation(deps.kotlin.reflect)
    implementation(deps.kotlin.serialization)
    compileOnly(deps.ksp.api)
    compileOnly(deps.ksp.impl)
}

val versionCatalog = extensions.getByType<VersionCatalogsExtension>().named("deps")
val koncatVer = versionCatalog.findVersion("koncatVer").get().requiredVersion
buildConfig {
    buildConfigField("String", "KONCAT_VERSION", "\"$koncatVer\"")
}