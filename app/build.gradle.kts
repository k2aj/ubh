import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0")
}

application {
    mainClass.set("ubh.App")
}

tasks {
    test {
        useJUnitPlatform()
        testLogging {
            showStandardStreams = true
            exceptionFormat = TestExceptionFormat.FULL
            events("passed", "skipped", "failed")
        }
    }
}