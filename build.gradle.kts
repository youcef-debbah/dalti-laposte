plugins {
    id("dz.jsoftware95.common-dependencies-android") version "1.7.3" apply false
    id("com.github.ben-manes.versions") version "0.40.0" apply false
}


println("root project config...")

buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.0.4")
        classpath("com.google.gms:google-services:4.3.10")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.8.1")
        classpath("com.google.firebase:perf-plugin:1.4.0")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.38.1")
        classpath("com.google.android.libraries.mapsplatform.secrets-gradle-plugin:secrets-gradle-plugin:2.0.0")
        // extra dependencies to resolve potential version conflicts
        classpath("com.google.guava:guava:30.1.1-jre")//31.0.1-jre
        classpath("com.android.tools.lint:lint-gradle:30.0.4")
    }
}

val lockPath: String? = System.getenv("COMMON_LIB_LOCK")
if (lockPath != null) {
    val lockFile = File(lockPath)
    while (lockFile.exists()) {
        println("configuration locked...")
        Thread.sleep(250)
    }
}