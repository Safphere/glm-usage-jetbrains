plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.intellij") version "1.17.4"
}

group = "com.safphere"
version = "0.1.9"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
}

// Configure Gradle IntelliJ Plugin
intellij {
    version.set("2024.2.5")
    // 不设置 type，兼容所有 JetBrains IDE
    plugins.set(listOf(/* Plugin Dependencies */))

    // 禁用自动更新 since/until build
    updateSinceUntilBuild.set(false)
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

    patchPluginXml {
        sinceBuild.set("191")
    }

    publishPlugin {
        token.set(providers.gradleProperty("publishToken").orElse(System.getenv("PUBLISH_TOKEN")))
        // 不签名直接发布
        channels.set(listOf("default"))
    }

    test {
        useJUnitPlatform()
    }
}
