import org.jetbrains.kotlin.konan.properties.loadProperties

plugins {
    id("org.jetbrains.intellij") version "0.4.19"
    java
    kotlin("jvm") version "1.3.72"
    id("de.fuerstenau.buildconfig") version "1.1.8"
    idea
}

val localProperties = loadProperties("local.properties")

group = "com.genovich.idea.idegram"
version = "0.1.1"

buildConfig {
    buildConfigField("int", "APP_ID", "${localProperties["com.genovich.idea.idegram.app_id"]}")
    buildConfigField("String", "API_HASH", "${localProperties["com.genovich.idea.idegram.api_hash"]}")
}

val publishPlugin by tasks.existing(org.jetbrains.intellij.tasks.PublishTask::class) {
    channels("alpha")
    token(localProperties["com.genovich.idea.idegram.publish_key"])
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(files("libs/td.jar"))
    testImplementation("junit", "junit", "4.12")
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version = "2020.1"
}
configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8

    sourceSets {
        main {
            java {
                srcDir(File(buildDir, "gen/buildconfig/src/main"))
            }
        }
    }
}
tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}