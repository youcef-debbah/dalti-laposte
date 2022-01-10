plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
    id("dagger.hilt.android.plugin")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
//    id("dz.jsoftware95.silverdocs") version "0.5.1"
//    id("dz.jsoftware95.silvercleaner-android") version "0.5.0"
    id("dz.jsoftware95.common-dependencies-android") version "1.7.3"
    id("com.github.ben-manes.versions") version "0.40.0"
}

println("admin config...")

android {
    compileSdk = 31

    buildFeatures.dataBinding = true

    val roomSchemaLocation = projectDir.absolutePath + "/src/androidTest/schemas"

    defaultConfig {
        minSdk = 21
        targetSdk = 31
        applicationId = "com.dalti.laposte.admin"
        versionCode = 11
        versionName = "0.9.11"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true

        javaCompileOptions {
            annotationProcessorOptions.argument("room.schemaLocation", roomSchemaLocation)
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildTypes {
        val defaultProguardFile = getDefaultProguardFile("proguard-android-optimize.txt")
        val proguardRules = File("proguard-rules.pro")

        named("debug") {
            ndk {
                debugSymbolLevel = "FULL"
            }
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(defaultProguardFile, proguardRules)
        }

        named("release") {
            ndk {
                debugSymbolLevel = "FULL"
            }
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(defaultProguardFile, proguardRules)
        }
    }
}

hilt {
    enableAggregatingTask = true
}

repositories {
    mavenLocal()
    google()
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":silverbox"))

//    implementation("com.github.andremion:counterfab:1.2.2")
//    implementation("com.android.support:appcompat-v7:+")
//    implementation("com.android.support:design:+")
//    implementation("com.android.support.constraint:constraint-layout:+")

    implementation("androidx.hilt:hilt-work:1.0.0")
    implementation("androidx.hilt:hilt-lifecycle-viewmodel:1.0.0-alpha03")
    annotationProcessor("androidx.hilt:hilt-compiler:1.0.0")

    implementation("com.google.dagger:hilt-android:2.40.5")
    annotationProcessor("com.google.dagger:hilt-android-compiler:2.40.5")
    addDaggerAll("2.40.5")

    addGuava()

    addJetbrainsAnnotations()
    addAndroidAnnotations()

    addGoogleMaterial()
    addRoomAll()
    addPaging()

    addEspressoAll()

    addJUnitTestExtToAndroidTests()

    addJunit4ToAllTests()
    addHamcrestToAllTests()
}