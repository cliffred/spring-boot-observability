import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.2.1"
	id("io.spring.dependency-management") version "1.1.4"
	kotlin("jvm") version "1.9.21"
	kotlin("plugin.spring") version "1.9.21"
}

group = "red.cliff"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
	mavenCentral()
}

dependencies {
	val logback = "0.1.5"
	val lokiAppender = "1.4.2"
	val kotest = "5.8.0"
	val kotestSpring = "1.1.3"
	val springMockk = "4.0.2"

	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.micrometer:micrometer-tracing-bridge-otel")
	implementation("io.opentelemetry:opentelemetry-exporter-otlp")

	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

	developmentOnly("org.springframework.boot:spring-boot-devtools")
	developmentOnly("org.springframework.boot:spring-boot-docker-compose")

	runtimeOnly("io.micrometer:micrometer-registry-prometheus")
	runtimeOnly("ch.qos.logback.contrib:logback-jackson:$logback")
	runtimeOnly("ch.qos.logback.contrib:logback-json-classic:$logback")
	runtimeOnly("com.github.loki4j:loki-logback-appender:$lokiAppender")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webflux")
	testImplementation("io.kotest:kotest-runner-junit5:$kotest")
	testImplementation("io.kotest:kotest-assertions-core:$kotest")
	testImplementation("io.kotest.extensions:kotest-extensions-spring:$kotestSpring")
	testImplementation("com.ninja-squad:springmockk:$springMockk")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xjsr305=strict"
		jvmTarget = "21"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
