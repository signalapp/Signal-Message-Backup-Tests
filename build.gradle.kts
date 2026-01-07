plugins {
  id("org.jetbrains.kotlin.jvm") version "2.1.0"
  id("com.squareup.wire") version "4.4.3"
  id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
  application
}

repositories {
  mavenCentral()
  mavenLocal()
  maven {
    name = "SignalBuildArtifacts"
    url = uri("https://build-artifacts.signal.org/libraries/maven/")
    content {
      includeGroupByRegex("org\\.signal.*")
    }
  }
}

wire {
  kotlin {}
}

application {
  mainClass = "MainKt"
}

dependencies {
  implementation("org.signal:libsignal-client:0.86.8")
  implementation("com.thedeanda:lorem:2.2")
  implementation(kotlin("reflect"))
}
