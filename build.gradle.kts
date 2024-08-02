plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "1.7.1"
    id("io.github.goooler.shadow") version "8.1.7"
}

group = "com.froobworld"
version = "1.5.7"

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

repositories {
    mavenLocal()
    mavenCentral()
    maven(url = "https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    paperweight.paperDevBundle("1.21-R0.1-SNAPSHOT")
    compileOnly("me.clip", "placeholderapi", "2.11.6")
    implementation("org.jooq", "joor-java-8", "0.9.14")
    implementation("com.froobworld", "nab-configuration", "1.0.2")
    implementation("org.bstats", "bstats-bukkit", "3.0.0")
}

tasks {
    compileJava {
        options.release = 21
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
    }

    shadowJar {
        relocate("com.froobworld.nabconfiguration", "com.froobworld.viewdistancetweaks.lib.nabconfiguration")
        relocate("org.joor", "com.froobworld.viewdistancetweaks.lib.joor")
        relocate("org.bstats", "com.froobworld.viewdistancetweaks.lib.bstats")
    }

    processResources {
        filesMatching("plugin.yml") {
            expand("version" to project.version)
        }
    }
}
