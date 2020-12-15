import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    application
    eclipse
}
java {
	sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0")
    implementation("org.hjson:hjson:3.0.0")
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

    jar {
        manifest {
            attributes["Main-Class"] = "ubh.App"
        }
        from(configurations.runtimeClasspath.get().map({ if (it.isDirectory) it else zipTree(it) }))
    }
}
