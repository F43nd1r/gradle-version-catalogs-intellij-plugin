plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.8.0"
    id("org.jetbrains.intellij") version "1.11.0"
    id("fr.brouillard.oss.gradle.jgitver") version "0.10.0-rc03"
}

group = "com.faendir"

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2022.2.1")
    type.set("IC")

    plugins.set(
        listOf(
            "org.toml.lang:222.3739.16",
            "com.intellij.gradle",
            "org.jetbrains.idea.reposearch",
            "org.jetbrains.kotlin",
            "com.intellij.java"
        )
    )
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    publishPlugin {
        token.set(project.findProperty("intellijToken") as? String ?: System.getenv("INTELLIJ_TOKEN"))
    }

    runIde {
        jvmArgs("-Xmx8G")
    }

    buildSearchableOptions {
        enabled = false
    }

    patchPluginXml {
        sinceBuild.set("222")
        untilBuild.set("")
    }

    listProductsReleases {
        types.addAll("IC", "AI")
    }
}

jgitver {
    regexVersionTag = "v([0-9]+(?:\\.[0-9]+){0,2}(?:-[a-zA-Z0-9\\-_]+)?)"
}
