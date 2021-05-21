import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin

plugins {
    id("de.itemis.mps.common-convention")
}

group = "de.itemis.mps"

val mpsVersion = "2020.3.3"
ext["mpsVersion"] = mpsVersion
val wrapperVersion = "0"

version = "$mpsVersion.$wrapperVersion-SNAPSHOT"

dependencies {
    implementation(kotlin("test"))
    implementation("com.xenomachina", "kotlin-argparser", "2.+")
    // TODO ???
    //this version needs to align with the version shiped with MPS found in the /lib folder otherwise, runtime problems will
    //surface because mismatching jars on the classpath.
    implementation("com.fasterxml.jackson.dataformat", "jackson-dataformat-xml", "2.+")
    implementation("org.apache.logging.log4j", "log4j-api","2.14.1")
    implementation("org.apache.logging.log4j", "log4j-core","2.14.1")
}
