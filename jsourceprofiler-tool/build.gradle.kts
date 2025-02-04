import com.vanniktech.maven.publish.SonatypeHost

plugins {
    java
    id("com.vanniktech.maven.publish") version "0.30.0"
    id("de.undercouch.download") version "5.6.0"
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
        group = "build"
        description = "Creates a single JAR file containing all dependencies (like the 'common' module)."
        archiveBaseName.set("profiler")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        manifest {
            attributes["Main-Class"] = mainClass
        }
        from(
            configurations.runtimeClasspath.get()
            .onEach { println("add from dependencies: ${it.name}") }
            .map { if (it.isDirectory) it else zipTree(it) })
        val sourcesMain = sourceSets.main.get()
        from(sourcesMain.output)
    }
}

val cocoUrl = "https://ssw.jku.at/Research/Projects/Coco/Java/Coco.jar"
val libsDir = layout.projectDirectory.dir("lib")
val cocoJar = libsDir.file("Coco.jar")
val grammarFile = layout.projectDirectory.file("src/main/coco/JavaFile.atg")
val generatedSourcesDir = layout.projectDirectory.dir("src/main/java/org/matwoess/jsourceprofiler/tool/instrument")
val generatedScanner = generatedSourcesDir.file("Scanner.java")
val generatedParser = generatedSourcesDir.file("Parser.java")
val generatedScannerOld = generatedSourcesDir.file("Scanner.java.old")
val generatedParserOld = generatedSourcesDir.file("Parser.java.old")

// Task to download Coco
val downloadCoco by tasks.registering(de.undercouch.gradle.tasks.download.Download::class) {
    description = "Downloads the Coco/R JAR library."
    src(cocoUrl)
    dest(cocoJar)
    overwrite(false)
    onlyIfModified(true)
    onlyIf { !cocoJar.asFile.exists() } // only attempt if library does not exist
    doFirst {
        libsDir.asFile.mkdirs() // Create lib directory if needed
    }
}
// Task to generate parser/scanner
val generateParser by tasks.registering(JavaExec::class) {
    group = "build"
    description = "Generates the Scanner.java and Parser.java files."
    dependsOn(downloadCoco)
    inputs.files(fileTree("src/main/coco"))
    outputs.files(generatedScanner, generatedParser)

    classpath = files(cocoJar)
    mainClass.set("Coco.Coco") // Main class in Coco.jar
    args = listOf(
        "-o", generatedSourcesDir.asFile.absolutePath,
        "-package", "org.matwoess.jsourceprofiler.tool.instrument",
        grammarFile.asFile.absolutePath
    )
}
// Make compilation depend on generation
tasks.compileJava {
    dependsOn(generateParser)
}
// Clean generated files
tasks.clean {
    delete(generatedScanner, generatedParser, generatedScannerOld, generatedParserOld)
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