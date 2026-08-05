plugins {
    id("java")
    kotlin("jvm")
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
    id("com.gradleup.shadow") version "8.3.6"
    id("xyz.jpenilla.run-paper") version "3.0.2"
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
    paperweight.paperDevBundle("26.2.build.92-stable")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testImplementation("com.github.seeseemelk:MockBukkit-v1.21:3.107.0")

    // compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    implementation(platform("com.intellectualsites.bom:bom-newest:1.55")) // Ref: https://github.com/IntellectualSites/bom
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Core")
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit") { isTransitive = false }
    implementation(kotlin("stdlib"))
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(25)
}

paperweight {
    javaLauncher = javaToolchains.launcherFor {
        // Minecraft 26.1+ requires Java 25 to run the server
        languageVersion = JavaLanguageVersion.of(25)
    }
}

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

tasks {
    shadowJar {
        // This names your output file "PluginName-1.0-all.jar"
        archiveClassifier.set("all")
        destinationDirectory.set(file("run/plugins"))

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
        dependsOn(shadowJar) // Make sure a fresh plugin jar is in run/plugins before the server starts
        minecraftVersion("26.2")

        downloadPlugins {
            modrinth("FastAsyncWorldEdit", "2.15.3")
        }

    }

}

tasks.withType(xyz.jpenilla.runtask.task.AbstractRun::class) {
    // Not using JvmVendorSpec.JETBRAINS here: JBR's JVMTI implementation crashes
    // spark's bundled async-profiler on startup (SIGSEGV in JvmtiEnv::GetClassMethods).
    // Standard hotswap (method-body edits) still works fine via the IntelliJ debugger
    // on a normal JDK; you just lose JBR's enhanced hotswap for structural changes.
    javaLauncher = javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(25)
    }
}