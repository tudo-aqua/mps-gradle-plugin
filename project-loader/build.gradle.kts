plugins {
    id("de.itemis.mps.mps-wrapper-convention")
}

val mpsVersion: String by ext

dependencies {
    compileOnly("com.jetbrains", "mps-core", mpsVersion)
    compileOnly("com.jetbrains", "mps-environment", mpsVersion)
    compileOnly("com.jetbrains", "mps-platform", mpsVersion)
    compileOnly("com.jetbrains", "mps-openapi", mpsVersion)
    compileOnly("com.jetbrains", "platform-api", mpsVersion)
    compileOnly("com.jetbrains", "util", mpsVersion)
    testImplementation("junit:junit:4.12")
    testImplementation("org.xmlunit:xmlunit-core:2.6.+")
}

publishing {
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
