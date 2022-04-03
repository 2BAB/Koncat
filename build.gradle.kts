plugins {
    `github-release`
}

subprojects {
    afterEvaluate {
        if (project.name.startsWith("koncat-")) {
            val versionCatalog = extensions.getByType<VersionCatalogsExtension>().named("deps")
            val koncatVer = versionCatalog.findVersion("koncatVer").get().requiredVersion
            version = koncatVer
            group = "me.2bab"
        }
    }
}
