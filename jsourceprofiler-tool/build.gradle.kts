import com.vanniktech.maven.publish.SonatypeHost

plugins {
    java
    id("com.vanniktech.maven.publish") version "0.30.0"
}

group = property("group") ?: ""
version = property("commonVersion") ?: "unknown"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
tasks.compileJava {
    options.release = 17
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(mapOf("path" to ":jsourceprofiler-common")))
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("com.github.stefanbirkner:system-lambda:1.2.1")
}

tasks.test {
    useJUnitPlatform()
    // TODO: remove as soon as replacement API https://bugs.openjdk.org/browse/JDK-8199704 is available
    jvmArgs("-Djava.security.manager=allow")
}

val mainClass = "org.matwoess.jsourceprofiler.tool.cli.Main"

tasks {
    register("fatJar", Jar::class.java) {
        archiveBaseName.set("profiler")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        manifest {
            attributes["Main-Class"] = mainClass
        }
        from(configurations.runtimeClasspath.get()
            .onEach { println("add from dependencies: ${it.name}") }
            .map { if (it.isDirectory) it else zipTree(it) })
        val sourcesMain = sourceSets.main.get()
        from(sourcesMain.output)
    }
}

publishing {
    repositories {
        maven {
            name = "githubPackages"
            url = uri("https://maven.pkg.github.com/matwoess/jsourceprofiler")
            credentials(PasswordCredentials::class)
        }
    }
}

mavenPublishing {
    signAllPublications()
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    pom {
        name.set("jsourceprofiler-tool")
        description.set("A command-line source code profiler for Java programs that generates HTML reports.")
        inceptionYear.set("2023")
        url.set("https://github.com/matwoess/jsourceprofiler/")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("matwoess")
                name.set("Mathias Wöß")
                url.set("https://github.com/matwoess/")
            }
        }
        scm {
            url.set("https://github.com/matwoess/jsourceprofiler/")
            connection.set("scm:git:git://github.com/matwoess/jsourceprofiler.git")
            developerConnection.set("scm:git:ssh://git@github.com/matwoess/jsourceprofiler.git")
        }
    }
}