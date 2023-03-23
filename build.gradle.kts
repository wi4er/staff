import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.7.0"
	id("io.spring.dependency-management") version "1.1.0"
	kotlin("jvm") version "1.7.22"
	kotlin("plugin.spring") version "1.7.22"

	jacoco
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17
val exposedVersion: String = "0.17.14"

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.jetbrains.kotlin:kotlin-reflect")

	// Web
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.google.code.gson:gson:2.10.1")

	// Authorization
	implementation("com.auth0:java-jwt:4.3.0")
	implementation("com.auth0:jwks-rsa:0.22.0")

	// Database
	implementation("org.jetbrains.exposed:exposed:$exposedVersion")
	implementation("org.flywaydb:flyway-core:9.16.0")
	implementation("org.postgresql:postgresql:42.5.4")
	implementation("com.zaxxer:HikariCP:5.0.1")

	// Tests
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

jacoco {
	toolVersion = "0.8.8"
	reportsDirectory.set(layout.buildDirectory.dir("customJacocoReportDir"))
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.test {
	finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
	dependsOn(tasks.test)
}