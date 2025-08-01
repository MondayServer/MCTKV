plugins {
    `java-library`
    `maven-publish`
    id("com.gradleup.shadow") version "8.3.8"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.18"
}

group = "me.paperxiang"
version = "0.1.0"
description = "The Annihilation"

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://jitpack.io")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://maven.citizensnpcs.co/repo")
    maven("https://repo.momirealms.net/releases/")
    maven("https://repo.onarandombox.com/content/groups/public/")
    maven("https://repo.extendedclip.com/releases/")
}

dependencies {
    paperweight.paperDevBundle("${properties["paper_version"]}")
    compileOnly("com.comphenix.protocol:ProtocolLib:${properties["protocol_lib_version"]}")
    compileOnly("de.tr7zw:item-nbt-api-plugin:${properties["nbt_api_version"]}")
    compileOnly("net.citizensnpcs:citizens-main:${properties["citizens_version"]}")
    compileOnly("net.momirealms:craft-engine-core:${properties["craft_engine_version"]}")
    compileOnly("net.momirealms:craft-engine-bukkit:${properties["craft_engine_version"]}")
    compileOnly("org.mvplugins.multiverse.core:multiverse-core:${properties["multiverse_version"]}")
    compileOnly("me.clip:placeholderapi:${properties["placeholder_api_version"]}")
    compileOnly("net.megavex:scoreboard-library-api:${properties["scoreboard_library_version"]}")
    runtimeOnly("net.megavex:scoreboard-library-implementation:${properties["scoreboard_library_version"]}")
    runtimeOnly("net.megavex:scoreboard-library-modern:${properties["scoreboard_library_version"]}:mojmap")
    implementation("fr.mrmicky:FastInv:${properties["fast_inv_version"]}")
}

artifacts {
    archives(tasks.shadowJar)
}

tasks {
    compileJava {
        options.release = 21
        options.encoding = "UTF-8"
    }
    javadoc {
        options.encoding = "UTF-8"
    }
    shadowJar {
        relocate("net.megavex.scoreboardlibrary", "me.paperxiang.shaded.scoreboardlibrary")
        relocate("fr.mrmicky.fastinv", "me.paperxiang.shaded.fastinv")
    }
}
