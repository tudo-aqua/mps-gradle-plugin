package de.itemis.mps.gradle

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.GradleException

class GitBasedVersioning {
    companion object{

        /**
         * Checks whenever git based versioning is available.
         * */
        fun isGitVersioningAvailable() : Boolean {
            try {
                val output = getCommandOutput("git --version")
                return output.startsWith("git version 2")
            } catch (ignored : Exception) {
                return false
            }
        }

        fun getGitShortCommitHash() : String {
            return getCommandOutput("git rev-parse --short HEAD").substring(0,7)
        }

        fun getGitCommitHash() : String {
            return getCommandOutput("git rev-parse HEAD")
        }

        fun getGitCommitCount() : Int {
            //Original Groovy code:
            //return getCommandOutput("git rev-list --count HEAD").toInteger();
            return Integer.valueOf(getCommandOutput("git rev-list --count HEAD").trim())
        }

        /**
         * Gets the current Git branch either from TC env parameter (for CI builds) or from git rev-parse command (for commandline builds)
         * with slashes ("/") replaced by dashes ("-"). If the branch name cannot be determined, throws GradleException.
         * Never empty, never null.
         *
         * @return the current branch name with slashes ("/") replaced by dashes ("-")
         * @throws org.gradle.api.GradleException if the branch name cannot be determined
         */
        fun getGitBranch() : String {
            val gitBranch : String
            val gitBranchTC = System.getenv("teamcity_build_branch")
            if (gitBranchTC != null && gitBranchTC.isNotEmpty()) {
                gitBranch = gitBranchTC
                println("Branch From TeamCity: $gitBranch")
            } else {
                gitBranch = getCommandOutput("git rev-parse --abbrev-ref HEAD")
                println("Branch From Git Commandline: $gitBranch")
            }

            if (gitBranch.isEmpty()) {
                throw GradleException("Could not determine Git branch name")
            }
            return gitBranch.replace("/", "-")
        }

        private fun getCommandOutput(command : String) : String {
            var cmd = command
            if (Os.isFamily(Os.FAMILY_WINDOWS)) {
                cmd = "cmd /c $command"
            }
            //Original Groovy code:
            //return command.execute().in.text.trim()
            return Runtime.getRuntime().exec(cmd).inputStream.bufferedReader().use { it.readText().trim() }
        }

        fun getVersion(major: String, minor : String) : String {
            return getVersion(getGitBranch(), major, minor, getGitCommitCount())
        }

        fun getVersion(major : Int, minor : Int) : String{
            return getVersion(getGitBranch(), major.toString(), minor.toString(), getGitCommitCount())
        }

        fun getVersion(branch : String, major : String, minor : String) : String{
            return getVersion(branch, major, minor, getGitCommitCount())
        }

       fun getVersionWithCount(major : String, minor : String, count : Int) : String {
            return getVersion(getGitBranch(), major, minor, count)
        }

        fun getVersion(branch : String, major : String, minor : String, count : Int) : String {
            val hash = getGitShortCommitHash()
            val baseVersion = "$major.$minor.$count.$hash"
            if (branch == "master" || branch == "HEAD" /*this happens in detached head situations*/) {
                return baseVersion
            }

            return "$branch.$baseVersion"
        }

        /**
         * Convenience method for creating version without maintenance branch prefix (i.e. if branch starts with "maintenance" or "mps")
         *
         * @param major
         * @param minor
         * @return
         */
        fun getVersionWithoutMaintenancePrefix(major : String, minor : String) : String {
            val branch = getGitBranch()
            if (branch.startsWith("maintenance") || branch.startsWith("mps")) {
                return getVersion("HEAD", major, minor)
            } else {
                return getVersion(major, minor)
            }
        }
    }
}
