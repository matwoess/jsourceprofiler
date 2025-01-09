plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
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
    implementation("io.github.mkpaz:atlantafx-base:2.0.1")
    implementation(project(mapOf("path" to ":jsourceprofiler-tool")))
    implementation(project(mapOf("path" to ":jsourceprofiler-common")))
}

javafx {
    modules = listOf("javafx.controls", "javafx.fxml")
}

application {
    mainModule.set("fxui")
    mainClass.set("fxui.Launcher")
}


val mainClass = "fxui.Launcher"

tasks {
    register("fatFxJar", Jar::class.java) {
        archiveClassifier.set("all")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        manifest {
            attributes["Main-Class"] = mainClass
        }
        from(configurations.runtimeClasspath.get()
            //.filter { !it.name.startsWith("javafx-") }
            .onEach { println("add from dependencies: ${it.name}") }
            .map { if (it.isDirectory) it else zipTree(it) })
        val sourcesMain = sourceSets.main.get()
        from(sourcesMain.output)
    }
}

fun getOsSuffix(osName: String): String = when {
    osName.contains("win") -> "win"
    osName.contains("mac") -> "mac"
    osName.contains("nux") || osName.contains("nix") -> "linux"
    else -> "unknown"
}

tasks.named<Zip>("distZip") {
    val osParam = project.findProperty("os") as String?
    if (osParam != null) {
        javafx.setPlatform(osParam)
    }
    val osName = System.getProperty("os.name").lowercase()
    val osSuffix = osParam ?: getOsSuffix(osName)
    archiveFileName.set("${project.name}-$version-$osSuffix.zip")
}