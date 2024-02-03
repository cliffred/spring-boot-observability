package gradle

import red.cliff.gradle.CalVer

data class GitState(
    /**
     * Version of current commit if versioned
     */
    val currentVersion: CalVer?,
    /**
     * Whether the repository has any uncommitted changes.
     */
    val clean: Boolean,
    /**
     * The most recent (based on ancestry, not time) tagged version from the current commit, if any.
     */
    val baseVersion: CalVer?,
    /**
     * Versions that are already used in tags.
     */
    val claimedVersions: Set<CalVer>
)
