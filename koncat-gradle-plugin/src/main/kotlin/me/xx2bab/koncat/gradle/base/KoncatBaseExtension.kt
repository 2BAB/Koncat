package me.xx2bab.koncat.gradle.base

import me._bab.koncat_gradle_plugin.BuildConfig
import me.xx2bab.koncat.contract.KoncatArgumentsContract
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import javax.inject.Inject


abstract class KoncatBaseExtension @Inject constructor(
    objects: ObjectFactory,
    layout: ProjectLayout
) {

    internal val mainProjectOutputDir = layout.buildDirectory
        .dir("intermediates")
        .map {
            it.dir("koncat")
        }

    internal fun argumentsContract(project: Project): KoncatArgumentsContract {
        return KoncatArgumentsContract(
            projectName = project.name,
            koncatVersion = BuildConfig.KONCAT_VERSION,
            gradlePlugins = project.plugins.map { it.toString().split("@")[0] },
            declaredAsMainProject = isMainProject.get(),
            variantAwareIntermediates = mainProjectOutputDir.get().asFile, // TODO: it may be consumed eagerly
        )
    }

    val isMainProject: Property<Boolean> = objects.property<Boolean>().convention(false)

    val ksp: KSPAction = objects.newInstance(KSPAction::class.java)

    fun ksp(action: Action<KSPAction>) {
        action.execute(ksp)
    }

    val kcp: KCPAction = objects.newInstance(KCPAction::class.java)

    fun kcp(action: Action<KCPAction>) {
        action.execute(kcp)
    }
}

abstract class KSPAction @Inject constructor(
    objects: ObjectFactory
) {

    val enabled: Property<Boolean> = objects.property<Boolean>().convention(true)

}

abstract class KCPAction @Inject constructor(
    objects: ObjectFactory
) {

    val enabled: Property<Boolean> = objects.property<Boolean>().convention(true)

}

