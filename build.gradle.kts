println("root project config...")

buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:4.2.2")
        classpath("com.google.gms:google-services:4.3.10")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.8.1")
        classpath("com.google.firebase:perf-plugin:1.4.0")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.38.1")
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