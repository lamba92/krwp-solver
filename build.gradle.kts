@file:Suppress("UNUSED_VARIABLE")

import com.jfrog.bintray.gradle.BintrayExtension
import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.KonanTarget.MINGW_X64
import org.jetbrains.kotlin.konan.target.KonanTarget.MINGW_X86

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

    windowsTargets publishOnlyOn OS.WINDOWS
    allButWindowsTargets publishOnlyOn OS.MAC

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

publishing {
    publications.named<MavenPublication>("kotlinMultiplatform") {
        publishOnlyOn(OS.MAC)
    }
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
    setPublications(
        *publishing.publications.withType<MavenPublication>()
            .filter { it.publicationTasks.all { it.isEnabled } }
            .map { it.name }
            .toTypedArray()
    )
    publish = true
}

@Suppress("unused")
fun KotlinDependencyHandler.coroutines(module: String, version: String) =
    "org.jetbrains.kotlinx:kotlinx-coroutines-$module:$version"

fun BintrayExtension.pkg(action: BintrayExtension.PackageConfig.() -> Unit) =
    pkg(closureOf(action))

fun BintrayExtension.PackageConfig.version(action: BintrayExtension.VersionConfig.() -> Unit) =
    version(closureOf(action))

val KotlinMultiplatformExtension.allButWindowsTargets
    get() = targets - windowsTargets

val KotlinMultiplatformExtension.windowsTargets
    get() = targets.filterIsInstance<KotlinNativeTarget>()
        .filter { it.konanTarget == MINGW_X86 || it.konanTarget == MINGW_X64 }

infix fun Iterable<KotlinTarget>.publishOnlyOn(os: OS) = configure(this) {
    mavenPublication { publishOnlyOn(os) }
}

enum class OS {
    LINUX, MAC, WINDOWS
}

val MavenPublication.publicationTasks
    get() = tasks.withType<AbstractPublishToMaven>()
        .filter { it.publication == this }

fun MavenPublication.publishOnlyOn(os: OS) = publicationTasks.forEach {
    it.onlyIf {
        when (os) {
            OS.LINUX -> OperatingSystem.current().isLinux
            OS.MAC -> OperatingSystem.current().isMacOsX
            OS.WINDOWS -> OperatingSystem.current().isWindows
        }
    }
}