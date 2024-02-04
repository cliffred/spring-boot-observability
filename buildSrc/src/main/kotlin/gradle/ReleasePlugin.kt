package red.cliff.gradle

import gradle.GitState
import gradle.GitStateSupplier
import gradle.ReleaseTask
import gradle.VersionCalculator
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.register
import java.io.File

/**
 * Release plugin
 *
 * Manages the project version using git tags.
 * Uses a CalVer version scheme with the pattern yyyy.MM.dd_#,
 * e.g. 2021.05.21_7 means the 7th release of 2021-05-21.
 *
 * Use the release task to create a new version git.
 */
class ReleasePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val repositoryProvider = project.provider { openRepo(project.rootDir) }
        val gitStateProvider =
            project.provider { GitStateSupplier(repositoryProvider.orNull) { CalVer.parse(it).getOrNull() }.getGitState() }
        val gitStateProperty =
            project.objects.property<GitState>().apply {
                set(gitStateProvider)
                finalizeValueOnRead()
            }

        val versionProvider = project.provider { VersionCalculator(gitStateProperty.get()).calVer() }
        val versionProperty =
            project.objects.property<CalVer>().apply {
                set(versionProvider)
                finalizeValueOnRead()
            }

        project.version = LazyVersion(versionProperty)

        project.tasks.register("currentVersion") {
            group = "release"
            doLast {
                logger.quiet("Version: ${project.version}")
            }
        }

        project.tasks.register<ReleaseTask>("release", repositoryProvider, gitStateProperty, versionProperty).configure {
            group = "release"
        }
    }

    private fun openRepo(dir: File): Repository? {
        return runCatching {
            val builder = FileRepositoryBuilder()
            builder.readEnvironment()
            builder.findGitDir(dir)
            checkNotNull(builder.gitDir) { "No .git directory found!" }
            builder.build()
        }.getOrNull()
    }
}

private class LazyVersion(private val versionProvider: Provider<CalVer>) {
    override fun toString() = versionProvider.get().toString()
}
