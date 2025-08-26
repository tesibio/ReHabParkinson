// build.gradle.kts (raíz)

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Plugins Android y Kotlin disponibles para todos los módulos
        classpath("com.android.tools.build:gradle:8.9.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.25")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
