package de.itemis.mps.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.apache.tools.ant.taskdefs.condition.Os

import java.awt.Desktop
import java.net.URI

class GetMpsInBrowser : DefaultTask() {

    @Input
    lateinit var version: String

    private fun getMajorPart() : String {
        val split = version.split("\\.")
        if (split.size == 2) {
            return version
        }

        return split.take(2).joinToString(".")
    }

    private fun getDownloadUrl() : URI
    {
        val major = getMajorPart()

        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            return URI("https://download.jetbrains.com/mps/${major}/MPS-${version}.exe")
        } else if (Os.isFamily(Os.FAMILY_MAC)) {
            return URI("https://download.jetbrains.com/mps/${major}/MPS-${version}-macos-jdk-bundled.dmg")
        } else if (Os.isFamily(Os.FAMILY_UNIX)) {
            return URI("https://download.jetbrains.com/mps/${major}/MPS-${version}.tar.gz")
        } else {
            System.out.print("Warning: could not determine OS downloading generic distribution")
            return URI("http://download.jetbrains.com/mps/${major}/MPS-${version}.zip")
        }
    }

    @TaskAction
    fun build() {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(getDownloadUrl())
        } else {
            throw GradleException("this task is not supported in headless mode")
        }
    }
}
