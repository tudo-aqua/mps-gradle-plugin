import de.itemis.mps.gradle.GitBasedVersioning
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

group = "de.itemis.mps"

plugins {
    kotlin("jvm")
    `maven-publish`
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
    maven {
        url = URI("https://projects.itemis.de/nexus/content/repositories/mbeddr")
    }
}

val nexusUsername: String? by project
val nexusPassword: String? by project

val kotlinArgParserVersion: String by project
val mpsVersion: String by project

val kotlinApiVersion: String by project
val kotlinVersion: String by project

val pluginVersion = "2"

version = if (project.hasProperty("forceCI") || project.hasProperty("teamcity")) {
    // maintenance builds for specific MPS versions should be published without branch prefix, so that they can be
    // resolved as dependency from the gradle plugin using version spec "de.itemis.mps:modelcheck:$mpsVersion+"
    GitBasedVersioning.getVersionWithoutMaintenancePrefix(mpsVersion, pluginVersion)
} else {
    "$mpsVersion.$pluginVersion-SNAPSHOT"
}


dependencies {
    implementation(kotlin("stdlib-jdk8", version = kotlinVersion))
    implementation("com.xenomachina:kotlin-argparser:$kotlinArgParserVersion")
    compileOnly("com.jetbrains:mps-openapi:$mpsVersion")
    compileOnly("com.jetbrains:mps-core:$mpsVersion")
    compileOnly("com.jetbrains:mps-tool:$mpsVersion")
    compileOnly("com.jetbrains:mps-messaging:$mpsVersion")
    compileOnly("com.jetbrains:platform-api:$mpsVersion")
    compileOnly("com.jetbrains:platform-concurrency:$mpsVersion")
    implementation(project(":project-loader"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.apiVersion = kotlinApiVersion
    kotlinOptions.allWarningsAsErrors = true
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