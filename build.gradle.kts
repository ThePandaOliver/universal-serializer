plugins {
	kotlin("jvm") version "2.2.20"
}

group = "dev.pandasystems"
version = "0.1.0-SNAPSHOT"

repositories {
	mavenCentral()
}

dependencies {
	testImplementation(kotlin("test"))

	implementation(kotlin("reflect"))

	// https://mvnrepository.com/artifact/com.google.guava/guava
	implementation("com.google.guava:guava:33.5.0-jre")
}

tasks.test {
	useJUnitPlatform()
}

kotlin {
	jvmToolchain(21)
}