plugins {
	kotlin("jvm") version "2.2.20"
	`maven-publish`
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
	jvmToolchain(17)
}

publishing {
	publications {
		create<MavenPublication>("maven") {
			from(components["kotlin"])
		}
	}

	repositories {
		maven("https://repo.pandasystems.dev/repository/maven-snapshots/") {
			name = "PandasRepository"
			credentials {
				username = System.getenv("NEXUS_USERNAME")
				password = System.getenv("NEXUS_PASSWORD")
			}
		}
	}
}