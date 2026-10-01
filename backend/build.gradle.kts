plugins {
    kotlin("jvm") version "2.4.20"
    id("io.ktor.plugin") version "3.6.0"
}

group = "com.example.instagramclone"
version = "0.0.1"

application {
    mainClass = "com.example.instagramclone.ApplicationKt"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-netty")
    implementation("ch.qos.logback:logback-classic:1.6.5")
}
