// ═══════════════════════════════════════════════════════════════════════
// Root build — shared configuration for all modules
// ═══════════════════════════════════════════════════════════════════════

plugins {
    id("org.springframework.boot") version "3.3.0" apply false
    id("io.spring.dependency-management") version "1.1.6" apply false
    kotlin("jvm") version "1.9.24" apply false
    kotlin("plugin.spring") version "1.9.24" apply false
    kotlin("plugin.jpa") version "1.9.24" apply false
    id("com.google.protobuf") version "0.9.4" apply false
}

allprojects {
    group = "com.example.collab"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

// Shared config for every Kotlin subproject
subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    // Common dependency versions
    ext["grpcVersion"]       = "1.62.2"
    ext["protobufVersion"]   = "3.25.3"
    ext["grpcKotlinVersion"] = "1.4.1"

    dependencies {
        // Every module gets Kotlin stdlib + coroutines
        "implementation"("org.jetbrains.kotlin:kotlin-reflect")
        "implementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "21"
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }
}
