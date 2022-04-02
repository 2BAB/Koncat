
import com.github.breadmoirai.githubreleaseplugin.GithubReleaseTask
import java.util.*

val taskName = "releaseArtifactsToGithub"
val artifacts = project.objects.listProperty<Directory>()
listOf("koncat-contract", "koncat-gradle-plugin", "koncat-processor-api").forEach {
    val libs = project.objects
        .directoryProperty()
        .fileValue(File(listOf(it, "build", "libs").joinToString(File.separator)))
    artifacts.add(libs)
}


// Temporary workaround for directory is not recognized by ReleaseAssets
gradle.taskGraph.whenReady {
    beforeTask {
        if (this is GithubReleaseTask) {
            this.setReleaseAssets(artifacts.get().flatMap { it.asFile.listFiles().map { it } })
        }
    }
}

val tokenFromEnv: String? = System.getenv("GH_DEV_TOKEN")
val token: String = if (!tokenFromEnv.isNullOrBlank()) {
    tokenFromEnv
} else if (project.rootProject.file("local.properties").exists()){
    val properties = Properties()
    properties.load(project.rootProject.file("local.properties").inputStream())
    properties.getProperty("github.devtoken")
} else {
    ""
}

val versionCatalog = extensions.getByType<VersionCatalogsExtension>().named("deps")
val koncatVer = versionCatalog.findVersion("koncatVer").get().requiredVersion
println(koncatVer)

val repo = "Koncat"
val tagBranch = "main"
val releaseNotes = ""
createGithubReleaseTaskInternal(artifacts, token, repo, tagBranch, koncatVer, releaseNotes)


fun createGithubReleaseTaskInternal(
    artifacts: ListProperty<Directory>,
    token: String,
    repo: String,
    tagBranch: String,
    version: String,
    releaseNotes: String
): TaskProvider<GithubReleaseTask> {
//    val id = version.replace(".", "")
    return project.tasks.register<GithubReleaseTask>("releaseArtifactsToGithub") {
        group = "publishing"
        setAuthorization("Token $token")
        setOwner("2bab")
        setRepo(repo)
        setTagName(version)
        setTargetCommitish(tagBranch)
        setReleaseName("v${version}")
        setBody(releaseNotes)
        setDraft(false)
        setPrerelease(false)
//        setReleaseAssets(artifacts)
        setOverwrite(true)
        setAllowUploadToExisting(true)
        setApiEndpoint("https://api.github.com")
        setDryRun(false)
    }
}

