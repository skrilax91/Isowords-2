import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.model.PluginDependency

plugins {
    `java-library`
    id("org.spongepowered.gradle.plugin") version "2.0.2"
}

group = "dev.devix"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        name = "bukkit-repo"
        url = uri("https://hub.spigotmc.org/nexus/content/groups/public/")
    }
}

dependencies {
    implementation("org.bukkit:bukkit:1.8-R0.1-SNAPSHOT")
    // shade "org.somewebsite:some-dependency:version"
}

sponge {
    apiVersion("8.1.0")
    license("MIT")
    loader {
        name(PluginLoaders.JAVA_PLAIN)
        version("1.0")
    }
    plugin("isoworlds2") {
        displayName("IsoWorlds 2")
        entrypoint("sponge.Main")
        description("Isoworlds is a large scale personal worlds manager")
        links {
            homepage("https://github.com/skrilax91/Isowords-2")
            source("https://github.com/skrilax91/Isowords-2")
            issues("https://github.com/skrilax91/Isowords-2/issues")
        }
        contributor("Devix") {
            description("Author")
        }
        dependency("spongeapi") {
            loadOrder(PluginDependency.LoadOrder.AFTER)
            optional(false)
        }
    }
}

val javaTarget = 8 // Sponge targets a minimum of Java 8
java {
    sourceCompatibility = JavaVersion.toVersion(javaTarget)
    targetCompatibility = JavaVersion.toVersion(javaTarget)
    if (JavaVersion.current() < JavaVersion.toVersion(javaTarget)) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(javaTarget))
    }
}

tasks.withType(JavaCompile::class).configureEach {
    options.apply {
        encoding = "utf-8" // Consistent source file encoding
        if (JavaVersion.current().isJava10Compatible) {
            release.set(javaTarget)
        }
    }
}

// Make sure all tasks which produce archives (jar, sources jar, javadoc jar, etc) produce more consistent output
tasks.withType(AbstractArchiveTask::class).configureEach {
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false
}