plugins {
    java
    id("com.vanniktech.maven.publish") version "0.30.0" apply false
}

tasks.register<Javadoc>("aggregateJavadoc") {
    title = "All Packages"
    source = files(subprojects.flatMap {
        // filter module-info.java files to avoid conflicts of multiple modules on same level
        it.sourceSets.main.get().allJava.filter { f -> f.name != "module-info.java" }
    }).asFileTree
    classpath = files(subprojects.flatMap { it.sourceSets.main.get().compileClasspath })
    setDestinationDir(file("${layout.buildDirectory.get()}/docs/javadoc"))
    options {
        this as StandardJavadocDocletOptions
        encoding = "UTF-8"
        memberLevel = JavadocMemberLevel.PROTECTED
        links = listOf(
            "https://docs.oracle.com/javase/21/docs/api/",
            "https://download.java.net/java/GA/javafx20.0.1/docs/api/"
        )
    }
}
