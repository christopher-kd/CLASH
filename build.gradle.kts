plugins {
    id("java")
    kotlin("jvm")
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
    id("com.gradleup.shadow") version "8.3.6"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "net.infinitygrid"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven("https://maven.enginehub.org/repo/")
}


dependencies {
    paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testImplementation("com.github.seeseemelk:MockBukkit-v1.21:3.107.0")

    // compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    implementation(platform("com.intellectualsites.bom:bom-newest:1.55")) // Ref: https://github.com/IntellectualSites/bom
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Core")
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit") { isTransitive = false }
    implementation(kotlin("stdlib"))
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

paperweight {
    javaLauncher = javaToolchains.launcherFor {
        // Example scenario:
        // Paper 1.17.1 was originally built with JDK 16 and the bundle
        // has not been updated to work with 21+ (but we want to compile with a 25 toolchain)
        languageVersion = JavaLanguageVersion.of(21)
    }
}

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

tasks {
    shadowJar {
        // This names your output file "PluginName-1.0-all.jar"
        archiveClassifier.set("all")
        destinationDirectory.set(file("C:\\Users\\99ctl\\Documents\\CLASH\\plugins"))

        // Relocation (Crucial Step!)
        // Temporarily commented out due to ASM/Kotlin incompatibility in this environment
        // relocate("kotlin.", "net.infinitygrid.clash.libs.kotlin.")
        // relocate("org.jetbrains.annotations.", "net.infinitygrid.clash.libs.annotations.")
        
        exclude("META-INF/maven/**")
        exclude("META-INF/proguard/**")
        exclude("META-INF/versions/**")
        
        mergeServiceFiles()
    }

    build {
        dependsOn(shadowJar) // Automatically run shadowJar when you click 'build'
    }

    runServer {
        minecraftVersion("1.21.11")

        downloadPlugins {
            modrinth("FastAsyncWorldEdit", "2.15.0")
        }

    }

}

tasks.withType(xyz.jpenilla.runtask.task.AbstractRun::class) {
    javaLauncher = javaToolchains.launcherFor {
        vendor = JvmVendorSpec.JETBRAINS
        languageVersion = JavaLanguageVersion.of(21)
    }
    jvmArgs("-XX:+AllowEnhancedClassRedefinition")
}