// build.gradle.kts (raíz)

plugins {
    // Plugins principales, solo se declaran aquí con versión
    id("com.android.application") version "8.9.1" apply false
    id("com.android.library") version "8.9.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.25" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
