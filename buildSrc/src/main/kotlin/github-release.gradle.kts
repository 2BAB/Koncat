
import com.github.breadmoirai.githubreleaseplugin.GithubReleaseTask
import java.util.*

val taskName = "releaseArtifactsToGithub"
val artifacts = mutableListOf<File>()
listOf("koncat-contract", "koncat-gradle-plugin", "koncat-processor-api").forEach {
    val libs = File(listOf(it, "build", "libs").joinToString(File.separator))
    artifacts.add(libs)
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

val repo = "Koncat"
val tagBranch = "main"
val releaseNotes = ""
createGithubReleaseTaskInternal(artifacts, token, repo, tagBranch, koncatVer, releaseNotes)

fun createGithubReleaseTaskInternal(
    artifacts: List<File>,
    token: String,
    repo: String,
    tagBranch: String,
    version: String,
    releaseNotes: String
): TaskProvider<GithubReleaseTask> {
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
        // The github release task will not run in dependent,
        // so we resolve all actual regular files from /libs directory eagerly here.
        setReleaseAssets(artifacts.flatMap { it.listFiles().map { it } })
        setOverwrite(true)
        setAllowUploadToExisting(true)
        setApiEndpoint("https://api.github.com")
        setDryRun(false)
    }
}

