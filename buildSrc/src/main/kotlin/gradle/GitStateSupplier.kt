package gradle

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.revwalk.filter.RevFilter
import red.cliff.gradle.CalVer

class GitStateSupplier(
    private val repository: Repository?,
    private val versionTagParser: (String) -> CalVer?,
) {
    private lateinit var repo: Repository

    fun getGitState(): GitState {
        repo = repository ?: return GitState(null, false, null, emptySet())

        val git = Git(repo)
        val clean = git.status().call().isClean

        RevWalk(repo).use { walk ->
            val tagsPerCommitId =
                repo.refDatabase.getRefsByPrefix(Constants.R_TAGS)
                    .groupBy({ walk.parseCommit(it.peeledObjectId ?: it.objectId) }, { Repository.shortenRefName(it.name) })

            val claimedVersions = tagsPerCommitId.values.flatMap { tags -> tags.mapNotNull { versionTagParser(it) } }.toSet()
            val headObjectId = repo.findRef("HEAD").objectId
            val headCommit = walk.parseCommit(headObjectId)
            val currentVersion = headCommit.latestVersion(tagsPerCommitId)
            val baseVersion =
                if (claimedVersions.isNotEmpty()) {
                    walk.apply {
                        markStart(headCommit)
                        revFilter = RevFilter.ALL
                    }.firstNotNullOfOrNull {
                        it.latestVersion(tagsPerCommitId)
                    }
                } else {
                    null
                }

            return GitState(
                currentVersion = currentVersion,
                clean = clean,
                baseVersion = baseVersion,
                claimedVersions = claimedVersions,
            )
        }
    }

    private fun RevCommit.latestVersion(tagsPerCommitId: Map<RevCommit, List<String>>): CalVer? =
        tagsPerCommitId[this]?.mapNotNull { versionTagParser(it) }?.maxOrNull()
}
