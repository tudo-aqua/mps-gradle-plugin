package de.itemis.mps.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Files

class BundleMacosJdk : DefaultTask() {
    companion object{
        //Original Groovy Code:
        //This is a copy of the createTempDir() method of org.codehaus.groovy.runtime.DefaultGroovyStaticMethods
        //It also replaced "groovy-generated" with "kotlin-generated"
        fun createTempDir(): File {
            val tempPath = Files.createTempDirectory("kotlin-generated-tmpdir-")
            return tempPath.toFile()
        }
    }
    @InputFile
    lateinit var rcpArtifact: File

    @InputFile
    lateinit var jdk: File

    @OutputFile
    lateinit var outputFile: File

    fun setRcpArtifact(file: Any) {
        this.rcpArtifact = project.file(file)
    }

    fun setJdk(file: Any) {
        this.jdk = project.file(file)
    }

    /**
     * Sets the {@link #jdk} property from a dependency, given as either a {@link Dependency} object or in dependency
     * notation.
     */
    fun setJdkDependency(jdkDependency: Any) {
        val dep = project.dependencies.create(jdkDependency)
        val files = project.configurations.detachedConfiguration(dep).resolve()
        if (files.size != 1) {
            throw GradleException(
                "Expected a single file for jdkDependency '$jdkDependency', got ${files.size} files"
            )
        }
        this.jdk = files.first()
    }

    fun setOutputFile(file: Any) {
        this.outputFile = project.file(file)
    }

    @TaskAction
    fun build() {
        //Original Groovy code:
        //File scriptsDir = File.createTempDir()
        //File tmpDir = File.createTempDir()
        val scriptsDir: File = createTempDir()
        val tmpDir: File = createTempDir()
        try {
            val scriptName = "bundle_macos_jdk.sh"
            BundledScripts.extractScriptsToDir(scriptsDir, scriptName)
            project.exec {
                executable(File(scriptsDir, scriptName))
                args(rcpArtifact, tmpDir, jdk, outputFile)
                workingDir(scriptsDir)
            }
        } finally {
            // Do not use File.deleteDir() because it follows symlinks!
            // (e.g. the symlink to /Applications inside tmpDir)
            project.exec {
                commandLine("rm", "-rf", scriptsDir, tmpDir)
            }
        }
    }
}
