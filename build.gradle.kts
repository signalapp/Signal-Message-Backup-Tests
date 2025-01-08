plugins {
  id("org.jetbrains.kotlin.jvm") version "1.9.10"
  id("com.squareup.wire") version "4.4.3"
  id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
  application
}

repositories {
  mavenCentral()
  mavenLocal()
}

wire {
  kotlin {}
}

application {
  mainClass = "MainKt"
}

dependencies {
  implementation("org.signal:libsignal-client:0.65.0")
  implementation("com.thedeanda:lorem:2.2")
  implementation(kotlin("reflect"))
}
