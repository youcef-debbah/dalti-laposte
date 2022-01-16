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

println("client config...")

android {
    compileSdk = 31

    buildFeatures.dataBinding = true

    val roomSchemaLocation = projectDir.absolutePath + "/src/androidTest/schemas"

    defaultConfig {
        minSdk = 21
        targetSdk = 31
        applicationId = "com.dalti.laposte.client"
        versionCode = 16
        versionName = "0.9.16"
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

//    signingConfigs {
//        register("release") {
//            storeFile = project.file("D:\\Data\\keystores\\LocalAndroidStore.jks")
//            storePassword = "serializable"
//            keyAlias = "acceptance"
//            keyPassword = "a-2021"
//            enableV1Signing = true
//        }
//    }

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
//            signingConfig = signingConfigs.getByName("release")
        }
    }
}

hilt {
    enableAggregatingTask = true
}

//configurations.all {
//    resolutionStrategy.cacheChangingModulesFor(0, TimeUnit.SECONDS)
//}

repositories {
    mavenLocal()
    google()
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":silverbox"))

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