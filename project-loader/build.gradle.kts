import de.itemis.mps.gradle.GitBasedVersioning
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    `maven-publish`
    `java-gradle-plugin`
}

group = "de.itemis.mps"

val mpsVersion: String by project
val kotlinArgParserVersion: String by project

val nexusUsername: String? by project
val nexusPassword: String? by project
val fastXmlJacksonVersion: String by project

val pluginVersion = "1"

version = if (project.hasProperty("forceCI") || project.hasProperty("teamcity")) {
    // maintenance builds for specific MPS versions should be published without branch prefix, so that they can be
    // resolved as dependency from the gradle plugin using version spec "de.itemis.mps:modelcheck:$mpsVersion+"
    GitBasedVersioning.getVersionWithoutMaintenancePrefix(mpsVersion, pluginVersion)
} else {
    "$mpsVersion.$pluginVersion-SNAPSHOT"
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://projects.itemis.de/nexus/content/repositories/mbeddr")
    }
}

dependencies {
    implementation("com.xenomachina:kotlin-argparser:$kotlinArgParserVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:$fastXmlJacksonVersion")
    compileOnly("com.jetbrains:mps-core:$mpsVersion")
    compileOnly("com.jetbrains:mps-environment:$mpsVersion")
    compileOnly("com.jetbrains:mps-platform:$mpsVersion")
    compileOnly("com.jetbrains:mps-openapi:$mpsVersion")
    compileOnly("com.jetbrains:platform-api:$mpsVersion")
    compileOnly("com.jetbrains:util:$mpsVersion")
    testImplementation("junit:junit:4.12")
    testImplementation("org.xmlunit:xmlunit-core:2.6.+")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
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

    publications {
        create<MavenPublication>("projectLoader") {
            from(components["java"])
            versionMapping {
                allVariants {
                    fromResolutionResult()
                }
            }
        }
    }
}
