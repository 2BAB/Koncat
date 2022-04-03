plugins {
    kotlin("jvm")
    id("com.github.gmazzo.buildconfig")
    `maven-central-publish`
}

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

val versionCatalog = extensions.getByType<VersionCatalogsExtension>().named("deps")
val koncatVer = versionCatalog.findVersion("koncatVer").get().requiredVersion
buildConfig {
    buildConfigField("String", "KONCAT_VERSION", "\"$koncatVer\"")
}