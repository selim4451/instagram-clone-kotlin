plugins {
    kotlin("jvm") version "2.4.20"
    kotlin("plugin.serialization") version "2.4.20"
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
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.ktor:ktor-server-status-pages")
    implementation("io.ktor:ktor-server-auth")
    implementation("io.ktor:ktor-server-auth-jwt")
    implementation("ch.qos.logback:logback-classic:1.6.5")
    implementation("org.postgresql:postgresql:42.7.13")
    implementation("com.zaxxer:HikariCP:7.1.0")
    implementation("at.favre.lib:bcrypt:0.10.2")
}
