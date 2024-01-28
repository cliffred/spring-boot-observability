import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21

plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.kotlinter)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.sonar)
    alias(libs.plugins.git.properties)
    id("jacoco")
}

group = "red.cliff"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = JVM_21
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(libs.spring.cloud.bom))
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jdbc)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.bundles.kotlin)
    implementation(libs.kotlin.logging)
    implementation(libs.bundles.logging)
    implementation(libs.bundles.tracing)
    implementation(libs.springdoc)

    developmentOnly(libs.spring.boot.devtools)
    developmentOnly(libs.spring.boot.docker.compose)

    runtimeOnly(libs.micrometer.registry.prometheus)
    runtimeOnly(libs.h2)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.bundles.kotest)
    testImplementation(libs.spring.mockk)
    testImplementation(libs.spring.security.test)
    testImplementation(libs.spring.boot.test.autoconfigure)
    testImplementation(libs.kotlin.wiremock)
    testImplementation(libs.spring.cloud.contract.wiremock)

    testRuntimeOnly(libs.spring.boot.starter.webflux)
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("PASSED", "SKIPPED", "FAILED")
    }
    systemProperty("kotest.tags", "!Manual")
}

sonar {
    properties {
        property("sonar.projectKey", "cliffred_spring-boot-observability")
        property("sonar.organization", "cliffred")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}

tasks.jacocoTestReport {
    reports {
        xml.required = true
    }
}

springBoot {
    buildInfo()
}
