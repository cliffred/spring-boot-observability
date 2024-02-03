package gradle

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask
import red.cliff.gradle.CalVer
import javax.inject.Inject

@UntrackedTask(because = "Tracked by git state")
abstract class ReleaseTask @Inject constructor(
    private val repositoryProvider: Provider<Repository?>,
    private val gitStateProvider: Provider<GitState>,
    private val versionProvider: Provider<CalVer>,
) : DefaultTask() {
    @TaskAction
    fun release() {
        val repository = repositoryProvider.orNull
        checkNotNull(repository) { "Can not release when not on a git repository." }

        val gitState = gitStateProvider.get()
        check(gitState.clean) { "Workspace should be clean to release." }

        val headReleased = gitState.currentVersion?.snapshot?.not() ?: false
        check(!headReleased) { "Current head is already released with version ${gitState.currentVersion}." }

        val releaseVersion = versionProvider.get().release()
        logger.lifecycle("Setting version tag $releaseVersion")

        repository.use { repo ->
            Git(repo).tag().setName(releaseVersion.toString()).setAnnotated(true).call()
        }
    }
}
