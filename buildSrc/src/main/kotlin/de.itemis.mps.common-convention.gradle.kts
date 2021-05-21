import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

plugins {
    `maven-publish`
    id("org.jetbrains.kotlin.jvm")
}

group = "de.itemis.mps"

val versionMajor = 2
val versionMinor = 0

val nexusUsername: String? by project
val nexusPassword: String? by project

version = "$versionMajor.$versionMinor-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        // TODO do we need this???
        url = URI("https://projects.itemis.de/nexus/content/repositories/mbeddr/")
    }
}

dependencyLocking {
    lockAllConfigurations()
}

publishing {
    repositories {
        maven {
            name = "itemis"
            url = uri("https://projects.itemis.de/nexus/content/repositories/mbeddr")
            credentials {
                username = nexusUsername
                password = nexusPassword
            }
        }
    }
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
        kotlinOptions.allWarningsAsErrors = true
    }

    register("resolveAndLockAll") {
        doFirst {
            require(gradle.startParameter.isWriteDependencyLocks)
        }
        doLast {
            configurations.filter { it.isCanBeResolved }.forEach { it.resolve() }
        }
    }
}