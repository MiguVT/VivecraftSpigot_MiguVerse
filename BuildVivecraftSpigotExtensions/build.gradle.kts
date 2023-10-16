import com.github.breadmoirai.githubreleaseplugin.GithubReleaseTask
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar


group = "org.vivecraft"
version = "2.1.1"

plugins {
    `java-library`
    id("com.github.breadmoirai.github-release") version "2.4.1"
    id("com.github.johnrengelman.shadow") version "7.1.0"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.2"
}

configurations {
    compileClasspath.get().extendsFrom(create("shadeOnly"))
}

// See https://github.com/Minecrell/plugin-yml
bukkit {
    name = "Vivecraft-Spigot-Extensions"
    main = "org.vivecraft.VSE"
    apiVersion = "1.19"
    website = "https://www.vivecraft.org"
    authors = listOf("jrbudda", "jaron780", "CJCrafter")
    prefix = "Vivecraft"
    softDepend = listOf("Vault")

    commands {
        register("Vive") {
            description = "Vivecraft Spigot Extensions"
            usage = "/vive <command>"
            aliases = listOf("vse")
        }
    }
}

// https://github.com/BreadMoirai/github-release-gradle-plugin
tasks.register<GithubReleaseTask>("createGithubRelease").configure {

    owner.set("CJCrafter")
    repo.set("Vivecraft_Spigot_Extensions")
    authorization.set("Token ${findProperty("pass").toString()}")
    tagName.set("${project.version}")
    targetCommitish.set("master")
    releaseName.set("${project.version}")
    draft.set(false)
    prerelease.set(false)
    generateReleaseNotes.set(true)
    body.set("")
    overwrite.set(false)
    allowUploadToExisting.set(false)
    apiEndpoint.set("https://api.github.com")

    setReleaseAssets(file("build/libs").listFiles())

    // If set to true, you can debug that this would do
    dryRun.set(false)

    doFirst {
        println("Creating GitHub release")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":")) // base project

    listOf("19_R3", "20_R1", "20_R2").forEach {
        implementation(project(":Vivecraft_1_$it", "reobf"))
    }
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
        options.release.set(16)
    }
}

// The shadowJar task builds a "fat jar" (a jar with all dependencies built in).
tasks.named<ShadowJar>("shadowJar") {

    archiveFileName.set("Vivecraft_Spigot_Extensions-${project.version}.jar")
    configurations = listOf(project.configurations["shadeOnly"], project.configurations["runtimeClasspath"])

    // This automatically "shades" (adds to jar) the bstats libs into the
    // org.vivecraft.bstats package.
    dependencies {
        include(project(":")) // base project

        listOf("19_R3", "20_R1").forEach {
            include(dependency(":Vivecraft_1_$it"))
        }

        relocate("org.bstats", "org.vivecraft.bstats") {
            include(dependency("org.bstats:"))
        }
    }
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}

