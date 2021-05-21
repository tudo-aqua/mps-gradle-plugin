plugins {
    id("de.itemis.mps.mps-wrapper-convention")
}

val mpsVersion : String by ext

dependencies {
    compileOnly("com.jetbrains", "mps-openapi", mpsVersion)
    compileOnly("com.jetbrains", "mps-core", mpsVersion)
    compileOnly("com.jetbrains", "mps-tool", mpsVersion)
    compileOnly("com.jetbrains", "mps-messaging", mpsVersion)
    compileOnly("com.jetbrains", "platform-api", mpsVersion)
    compileOnly("com.jetbrains", "platform-concurrency", mpsVersion)
    implementation(project(":project-loader"))
}