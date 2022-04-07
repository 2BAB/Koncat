package me.xx2bab.koncat.contract

enum class KoncatArgument(val desc: String, val required: Boolean) {

    PROJECT_NAME("The current working Gradle Project.", true),
    KONCAT_VERSION("The Koncat Gradle Plugin version.", true),
    GRADLE_PLUGINS("All declared Gradle Plugins from current Project.", true),
    VARIANT_AWARE_INTERMEDIATES("The Koncat intermediates directory " +
            "which has multiple variant-aware sub directories.", true),
    DECLARED_AS_MAIN_PROJECT("If the current project is declared as Main Project " +
            "that will aggregate resources from all subprojects.", true)

}