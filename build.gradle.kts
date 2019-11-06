@file:Suppress("UNUSED_VARIABLE")

import com.jfrog.bintray.gradle.BintrayExtension
import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

plugins {
    kotlin("multiplatform") version "1.3.50"
    id("com.jfrog.bintray") version "1.8.4"
    `maven-publish`
}

extensions.findByName("buildScan")?.withGroovyBuilder {
    setProperty("termsOfServiceUrl", "https://gradle.com/terms-of-service")
    setProperty("termsOfServiceAgree", "yes")
}

repositories {
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
    jcenter()
}
group = "com.github.lamba92"
version = System.getenv("TRAVIS_TAG") ?: "0.0.1"

kotlin {
    jvm()
    js()
    mingwX64("windows-x64")
    linuxX64("linux-x64")
    iosArm64("ios-arm64")
    iosArm32("ios-arm32")
    iosX64("ios-x64")
    macosX64("macos-x64")

    val coroutinesVersion = "1.3.2"

    sourceSets["commonMain"].dependencies {
        implementation(kotlin("stdlib-common"))
        implementation(coroutines("core-common", coroutinesVersion))
    }

    sourceSets["jvmMain"].dependencies {
        implementation(kotlin("stdlib-jdk8"))
        implementation(coroutines("core", coroutinesVersion))
    }

    sourceSets["jsMain"].dependencies {
        implementation(kotlin("stdlib-js"))
        implementation(coroutines("core-js", coroutinesVersion))
    }

    sourceSets["windows-x64Main"].dependencies {
        implementation(coroutines("core-windowsx64", coroutinesVersion))
    }

    sourceSets["linux-x64Main"].dependencies {
        implementation(coroutines("core-linuxx64", coroutinesVersion))
    }

    sourceSets["ios-arm64Main"].dependencies {
        implementation(coroutines("core-iosarm64", coroutinesVersion))
    }

    sourceSets["ios-arm32Main"].dependencies {
        implementation(coroutines("core-iosarm32", coroutinesVersion))
    }

    sourceSets["ios-x64Main"].dependencies {
        implementation(coroutines("core-iosx64", coroutinesVersion))
    }

    sourceSets["macos-x64Main"].dependencies {
        implementation(coroutines("core-macosx64", coroutinesVersion))
    }

}

fun searchProperty(name: String) =
    project.findProperty(name) as String? ?: try {
        System.getenv(name)
    } catch (e: Throwable) {
        null
    }

bintray {
    user = searchProperty("bintrayUsername")
    key = searchProperty("bintrayApiKey")
    pkg {
        version {
            name = project.version.toString()
        }
        repo = "com.github.lamba92"
        name = "krwp-solver"
        setLicenses("Apache-2.0")
        vcsUrl = "https://github.com/lamba92/krwp-solver"
        issueTrackerUrl = "https://github.com/lamba92/krwp-solver/issues"
    }
    publish = true
    if (OperatingSystem.current().isMacOsX)
        setPublications(
            "js",
            "jvm",
            "kotlinMultiplatform",
            "linux-x64",
            "metadata",
            "ios-x64",
            "ios-arm32",
            "ios-arm64",
            "macos-x64"
        )
    else if (OperatingSystem.current().isWindows)
        setPublications("windows-x64")
}

@Suppress("unused")
fun KotlinDependencyHandler.coroutines(module: String, version: String) =
    "org.jetbrains.kotlinx:kotlinx-coroutines-$module:$version"

fun BintrayExtension.pkg(action: BintrayExtension.PackageConfig.() -> Unit) =
    pkg(closureOf(action))

fun BintrayExtension.PackageConfig.version(action: BintrayExtension.VersionConfig.() -> Unit) =
    version(closureOf(action))