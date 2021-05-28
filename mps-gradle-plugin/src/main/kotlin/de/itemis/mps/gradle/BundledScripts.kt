package de.itemis.mps.gradle

import org.gradle.api.GradleException
import java.io.File

import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermissions

class BundledScripts {
    companion object{
        fun extractScriptsToDir(dir : File, vararg scriptNames : String) {
            val rwxPermissions = PosixFilePermissions.fromString("rwx------")

            for (name in scriptNames) {
                val file = File(dir, name)
                if (!file.parentFile.isDirectory && ! file.parentFile.mkdirs()) {
                    throw GradleException("Could not create directory " + file.parentFile)
                }
                //Original Groovy code:
                //InputStream resourceStream = BundledScripts.class.getResourceAsStream(name)
                val resourceStream = BundledScripts::class.java.getResourceAsStream(name)
                if (resourceStream == null) {
                    throw IllegalArgumentException("Resource $name was not found")
                }
                //Original Groovy code:
                //resourceStream.withStream { is -> file.newOutputStream().withStream { os -> os << is } }
                resourceStream.transferTo(file.outputStream())
                Files.setPosixFilePermissions(file.toPath(), rwxPermissions)
            }
        }
    }
}
