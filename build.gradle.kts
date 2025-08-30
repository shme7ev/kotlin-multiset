plugins {
    kotlin("jvm") version "1.8.0"
    `maven-publish`
}

group = "io.github"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))
    testImplementation(kotlin("test"))
    testImplementation("junit:junit:4.13.2")
}

tasks.test {
    useJUnit()
}

kotlin {
    jvmToolchain(8)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "io.github"
            artifactId = "multiset"
            version = "1.0.0"

            from(components["java"])
        }
    }
}
