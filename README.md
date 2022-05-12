# Koncat


[![Maven Central](https://maven-badges.herokuapp.com/maven-central/me.2bab/koncat-processor-api/badge.svg)](https://search.maven.org/artifact/me.2bab/koncat-processor-api) 
[![Actions Status](https://github.com/2bab/Koncat/workflows/CI/badge.svg)](https://github.com/2bab/Koncat/actions) 
[![Apache 2](https://img.shields.io/badge/License-Apache%202-brightgreen.svg)](https://www.apache.org/licenses/LICENSE-2.0)

Aggregate Kotlin Symbols based on KSP for multi-modules development in compile-time. For instance, when you want to gather all implementations of an interface across multi-modules, Koncat must be the tool your shouldn't miss.

## Usage

**0x01. Add the plugin to classpath:**

``` kotlin
// Option 1.
// Add `mavenCentral` to `pluginManagement{}` on settings.gradle.kts,
// and koncat plugins ids.
pluginManagement {
	val koncatVer = "2.0.1"
	repositories {
        ...
        mavenCentral()
    }
    plugins {
    	...
    	id ("me.2bab.koncat.android.app") version koncatVer apply false
        id ("me.2bab.koncat.android.lib") version koncatVer apply false
        id ("me.2bab.koncat.jvm") version koncatVer apply false
    }
}


// Option 2.
// Using classic `buildscript{}` block in root build.gradle.kts.
buildscript {
    repositories {
        ...
        mavenCentral()
    }
    dependencies {
    	...
        classpath("me.2bab:koncat-gradle-plugin:2.0.1")
    }
}
```

**0x02. Add Koncat Gradle Plugins, and config the Koncat DSL for per module:**

Where you applied KSP plugin should append the `me.2bab.koncat.*` plugin as well.

``` kotlin
// For Android Application module
plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.devtools.ksp")
    // .android.app plugin will set `declaredAsMainProject` as true by default
    id("me.2bab.koncat.android.app")  <--
}

// For Android Library module
plugins {
    id("com.android.library")
    kotlin("android")
    id("com.google.devtools.ksp")
    id("me.2bab.koncat.android.lib")  <--
}

// For JVM library module
plugins {
    kotlin("jvm") // or `java`, `groovy` plugins, etc.
    id("com.google.devtools.ksp")
    id("me.2bab.koncat.jvm")  <--
}


// Common DSL for all above plugins
koncat {
    /**
     * To specify classes that annotated by the annotation list below should be aggregated.
     * Anonymous classes are not supported.
     */
    annotations.addAll("me.xx2bab.koncat.sample.annotation.ExportActivity")

    /**
     * To specify top-level classes that extend or implement from supertype list below
     * should be aggregated. Indirect type search are supported.
     * For example, `android.app.Activity` is passed into [classTypes],
     * so that `BaseActivity` `MainActivity` which are implementations of `Activity` will be aggregated still.
     */
    classTypes.addAll("me.xx2bab.koncat.sample.interfaze.DummyAPI")

    /**
     * To specify top-level properties that are declared as one of the type list below
     * should be aggregated. Indirect type search are supported.
     */
    propertyTypes.addAll("org.koin.core.module.Module")
    
    
    /**
     * To declare current working project (Gradle module in another word) as Main Project,
     * the Main Project will collect all Koncat metadata from dependencies.
     */
    val declaredAsMainProject: Property<Boolean> = objects.property<Boolean>().convention(false)
}
```

**0x03. Add koncat-processor & runtime APIs, and build your App!:**

``` kotlin
dependencies {
    ksp("me.2bab:koncat-processor:${sameVersionAsPlugin}")
    implementation("me.2bab:koncat-runtime:${sameVersionAsPlugin}")
}
```

The DSL configuration of `koncat{}` will be working with below APIs to retrieve aggregations.

``` kotlin
val koncat = Koncat()

// Case 1: check an Activity permission request before you navigate to Koncat#getAnnotatedClasses(...)
val libActivityMemberLvRequirement = koncat.getAnnotatedClasses(ExportActivity::class)!!
    .first { it.name == "me.xx2bab.koncat.sample.android.AndroidLibraryActivity" }
    .annotations
    .first { it.name == "me.xx2bab.koncat.sample.annotation.MemberRequired" }
    .arguments["level"]


// Case 2: register or run a set of services together with Koncat#getTypedClasses(...)
val collectedInterfaces = koncat.getTypedClasses(DummyAPI::class)!!.map { constructor ->
    constructor().onCall("...")
}


// Case 3: setup Koin modules with Koncat#getTypedProperties(...)
startKoin {
    modules(koncat.getTypedProperties(Module::class) ?: listOf())
}
```

Check more on [here](./sample/app/src/main/kotlin/me/xx2bab/koncat/sample).

**0x04. (Optional) Custom the Koncat final class generation:**

Firstly, enable `generateExtensionClass` to export metadata from Koncat. You can also disable `generateAggregationClass` for default aggregation class generation if you don't use it anymore. (It will invalid the function of `Koncat` runtime API as well). 

``` kotlin
koncat {
    /**
     * To enable/disable the Aggregation Class generation.
     * The Aggregation Class is actually `me.xx2bab.koncat.runtime.KoncatAggregation`,
     * that will be used by `koncat-runtime` library in runtime,
     * to replace the `koncat-stub` one which is an empty & compile-only placeholder.
     */
    val generateAggregationClass: Property<Boolean> = objects.property<Boolean>().convention(true)

    /**
     * To enable/disable the Extension Class generation.
     * The Extension Class is actually `me.xx2bab.koncat.runtime.KoncatAggregatedMeta`,
     * that will be used by 3rd party developers to customize the process of aggregated metadata.
     * For example, to generate a custom Aggregation Class, or to generate an API/Route report
     * during compile time.
     */
    val generateExtensionClass: Property<Boolean> = objects.property<Boolean>().convention(false)
}
```

Secondly, create your own processor project and add `koncat-process-api` to your dependencies:

``` kotlin
dependencies {
    implementation("me.2bab:koncat-processor-api:$latestVersion")
}
```

Then you should construct a KoncatProcAPI to your processor, Koncat will deal with the aggregating procedure and pass the final result to your custom processor:

``` kotlin
class ExtensionProcessorProvider : SymbolProcessorProvider {
    override fun create(
        environment: SymbolProcessorEnvironment
    ): SymbolProcessor {
        return ExtensionProcessor(
            environment.codeGenerator,
            environment.logger,
            KoncatProcAPIImpl(KSPAdapter(environment))  ①
        )
    }
}

class ExtensionProcessor(
    ...
    private val koncat: KoncatProcAPI
) : SymbolProcessor {

    private var holder: KoncatProcMetadataHolder? = null

    override fun process(resolver: Resolver): List<KSAnnotated> {
        holder = koncat.syncAggregatedMetadata(resolver)  ②
        return emptyList()
    }

    @OptIn(KotlinPoetKspPreview::class)
    override fun finish {
        super.finish()
        holder?.apply {  ③
            val fileSpec = RouterClassBuilder(resolve()).build()
            fileSpec.writeTo(codeGenerator, Dependencies(false, dependency))
        }
    }

    inner class RouterClassBuilder(
        private val data: KoncatProcMetadata
    ) {
        fun build(): FileSpec {
            val exportAPIs = data.typedClasses["me.xx2bab.koncat.sample.interfaze.DummyAPI"]!!
                .joinToString(separator = ", ") { "\"$it\"" }
            ...
        }
    }
}
```

- ① Initialize `KoncatProcAPI` by passing the `KSPAdapter` with current `SymbolProcessorEnvironment`.
- ② When running on main project, koncat helps aggregate all intermediates from sub projects by `Koncat#syncAggregatedMetadata()`. To support multi-rounds process, we need to retain the latest one in a holder.
- ③ On `finish()`, retrieve the latest `KoncatProcMetadataHolder`, and then 
    + Call `resolve()` to get the real `KoncatProcMetadata` object.
    + Pass the built-in `dependency` to `Dependencies(...)`

Lastly, add the custom-processor to your main project(Android Application for example):

``` kotlin
dependencies {
    ksp("com.company:custom-processor:$procVersion")
}
```

Check more on [here](./sample/custom-processor/src/main/kotlin/me/xx2bab/koncat/sample/kotlin).

## Compatible

ScratchPaper is only supported & tested on LATEST 2 Minor versions of Android Gradle Plugin and KSP. 

Koncat (Per minor version) |Suggested Env
-----------|-----------------
2.0.x | AGP 7.1/7.2 x KSP 1.6.21-1.0.5
1.0.x | AGP 7.1/7.2 x KSP 1.6.10-1.0.4


## Why Koncat?

A few precondition for Koncat used scenarios:

1. The project has multiple Gradle modules.
2. It requies to gather all meta info of annotated elements, for example a permission anntation on an Activity.
3. And later generate a aggregated class for quering/reporting/etc.

If DI frameworks suits well with current project, for example using the `Multibinding` feature from Koin/Dagger/Hilt, then Koncat is not necessary for it.

Koncat runs in compiler time, enhance the annotation processor capability:

- It can save hundreds of millseconds for launch-time of the app comparing to the runtime aggregation.
- It would be much earsier to generate source code file during AnnotationProcessor stage comparing to generate byte code during transforming stage.


## Git Commit Check

Check this [link](https://medium.com/walmartlabs/check-out-these-5-git-tips-before-your-next-commit-c1c7a5ae34d1) to make sure everyone will make a **meaningful** commit message.

So far we haven't added any hook tool, but follow the regex below:

```
(chore|feature|doc|fix|refactor|style|test|hack|release|clean)(:)( )(.{0,80})
```


## License

>
> Copyright 2022 2BAB
>
>Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
>
>   http://www.apache.org/licenses/LICENSE-2.0
>
> Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

