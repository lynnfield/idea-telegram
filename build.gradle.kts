import org.jetbrains.kotlin.konan.properties.loadProperties

plugins {
    id("org.jetbrains.intellij") version "0.4.19"
    java
    kotlin("jvm") version "1.3.72"
    id("de.fuerstenau.buildconfig") version "1.1.8"
    idea
}

val localProperties = loadProperties("local.properties")

group = "com.tnl.idea.telegram"
version = "1.0-SNAPSHOT"

buildConfig {
    buildConfigField("int", "APP_ID", "${localProperties["com.tnl.idea.telegram.app_id"]}")
    buildConfigField("String", "API_HASH", "${localProperties["com.tnl.idea.telegram.api_hash"]}")
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
    sourceSets.getByName("main").java {
        srcDir(File(buildDir, "gen/buildconfig/src/main"))
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
tasks.getByName<org.jetbrains.intellij.tasks.PatchPluginXmlTask>("patchPluginXml") {
    changeNotes(
        """
      Add change notes here.<br>
      <em>most HTML tags may be used</em>"""
    )
}