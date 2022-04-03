plugins {
    `kotlin-dsl`
    id("com.github.gmazzo.buildconfig")
    `maven-central-publish`
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation(gradleApi())
    implementation(projects.koncatContract)
    implementation(deps.kotlin.std)
    implementation(deps.polyfill.main)
    compileOnly(deps.android.gradle.plugin)
    compileOnly(deps.ksp.gradle)
}

gradlePlugin {
    plugins.register("koncat-gradle-plugin-android-app") {
        id = "me.2bab.koncat.android.app"
        implementationClass = "me.xx2bab.koncat.gradle.KoncatAndroidApplicationPlugin"
        displayName = "me.2bab.koncat.android.app"
    }
    plugins.register("koncat-gradle-plugin-android-lib") {
        id = "me.2bab.koncat.android.lib"
        implementationClass = "me.xx2bab.koncat.gradle.KoncatAndroidLibraryPlugin"
        displayName = "me.2bab.koncat.android.lib"
    }
    plugins.register("koncat-gradle-plugin-jvm") {
        id = "me.2bab.koncat.jvm"
        implementationClass = "me.xx2bab.koncat.gradle.KoncatJVMLibraryPlugin"
        displayName = "me.2bab.koncat.jvm"
    }
}

val versionCatalog = extensions.getByType<VersionCatalogsExtension>().named("deps")
val koncatVer = versionCatalog.findVersion("koncatVer").get().requiredVersion
buildConfig {
    buildConfigField("String", "KONCAT_VERSION", "\"$koncatVer\"")
}
