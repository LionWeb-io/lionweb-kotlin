plugins {
    alias(libs.plugins.release)
    alias(libs.plugins.superPublish) apply(false)
    alias(libs.plugins.kotlinJvm) apply(false)
    alias(libs.plugins.ktlint)
}

repositories {
    mavenCentral()
}

allprojects {

    group = "io.lionweb.lionweb-kotlin"
    project.version = version

    repositories {
        mavenLocal()
        mavenCentral()
    }
}

release {
    git {
        requireBranch.set("main")
        pushToRemote.set("origin")
    }
}

tasks.wrapper {
    gradleVersion = "8.8"
    // You can either download the binary-only version of Gradle (BIN) or
    // the full version (with sources and documentation) of Gradle (ALL)
    distributionType = Wrapper.DistributionType.ALL
}
