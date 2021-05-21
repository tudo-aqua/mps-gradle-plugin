plugins {
    id("de.itemis.mps.mps-wrapper-convention")
}

val mpsVersion: String by ext

dependencies {
    implementation(kotlin("test"))
    compileOnly("com.jetbrains", "mps-openapi", mpsVersion)
    compileOnly("com.jetbrains", "mps-core", mpsVersion)
    compileOnly("com.jetbrains", "mps-modelchecker", mpsVersion)
    compileOnly("com.jetbrains", "mps-httpsupport-runtime", mpsVersion)
    compileOnly("com.jetbrains", "mps-project-check", mpsVersion)
    compileOnly("com.jetbrains", "mps-platform", mpsVersion)
    compileOnly("com.jetbrains", "platform-api", mpsVersion)
    compileOnly("com.jetbrains", "extensions", mpsVersion)
    compileOnly("com.jetbrains", "util", mpsVersion)
    implementation(project(":project-loader"))
}

publishing {
    publications {
        create<MavenPublication>("modelcheck") {
            from(components["java"])
            versionMapping {
                allVariants {
                    fromResolutionResult()
                }
            }
        }
    }
}