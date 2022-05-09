plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation(deps.kotlin.std)
    implementation(deps.kotlin.serialization)
    implementation(deps.kotlinpoet.core)
    implementation(deps.kotlinpoet.ksp)
    implementation(deps.ksp.api)
    api(projects.koncatProcessorApi)
    implementation(projects.koncatRuntimeModel)
}

testing {
    suites {
        val integrationTest by registering(JvmTestSuite::class) {
            useJUnitJupiter()
            testType.set(TestSuiteType.INTEGRATION_TEST)
            dependencies {
                implementation(project)
                implementation(deps.hamcrest)
                implementation(deps.kotlin.compile.testing)
                implementation(deps.kotlin.compile.testing.ksp)
                implementation(deps.kotlin.serialization)
            }
        }
    }
}

tasks.named("check") {
    dependsOn(testing.suites.named("integrationTest"))
}

tasks.withType<Test> {
    testLogging {
        this.showStandardStreams = true
    }
}