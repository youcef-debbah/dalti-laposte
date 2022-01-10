plugins {
    id("com.android.library")
    id("dagger.hilt.android.plugin")
//    id("dz.jsoftware95.silverdocs") version "0.5.1"
//    id("dz.jsoftware95.silvercleaner-android") version "0.5.0"
    id("dz.jsoftware95.common-dependencies-android") version "1.7.3"
    id("com.github.ben-manes.versions") version "0.40.0"
}

println("core library config...")

val hostname: String by lazy { getLocalIPv4() ?: "192.168.1.6" }

android {
    compileSdk = 31

    buildFeatures.dataBinding = true
    val roomSchemaLocation = projectDir.absolutePath + "/src/androidTest/schemas"

    defaultConfig {
        minSdk = 21
        targetSdk = 31
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
            isMinifyEnabled = false
            proguardFiles(defaultProguardFile, proguardRules)
            resValue("string", "hostname", hostname)
            resValue("string", "build_epoch", "${System.currentTimeMillis()}")
            resValue("bool", "is_development_stage", "true")
        }

        named("release") {
            ndk {
                debugSymbolLevel = "FULL"
            }
            isMinifyEnabled = true
            proguardFiles(defaultProguardFile, proguardRules)
            resValue("string", "hostname", "www.dalti-laposte.com")
            resValue("string", "build_epoch", "${System.currentTimeMillis()}")
            resValue("bool", "is_development_stage", "false")
        }
    }
}

repositories {
    mavenLocal()
    google()
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation(project(":silverbox"))

    api("dz.jsoftware95:common:0.7.+")
    api("dz.jsoftware95:cleaningtools:0.7.0")
    api("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    api("com.google.code.findbugs:jsr305:3.0.2")
    api("androidx.appcompat:appcompat:1.4.0")
    api("androidx.webkit:webkit:1.4.0")
    api("androidx.constraintlayout:constraintlayout:2.1.2")
    api("com.google.android.flexbox:flexbox:3.0.0")
    api("com.google.android.material:material:1.5.0-rc01")
    api("androidx.lifecycle:lifecycle-livedata:2.3.1")
    api("androidx.lifecycle:lifecycle-viewmodel:2.3.1")
    api("androidx.lifecycle:lifecycle-service:2.3.1")
    api("net.objecthunter:exp4j:0.4.8")

    api(platform("com.google.firebase:firebase-bom:29.0.0"))
    api("com.google.firebase:firebase-appcheck-safetynet:16.0.0-beta04")
    api("com.google.firebase:firebase-analytics")
    api("com.google.firebase:firebase-crashlytics")
    api("com.google.firebase:firebase-inappmessaging-display")
    api("com.google.firebase:firebase-messaging")
    api("com.google.firebase:firebase-config")
    api("com.google.firebase:firebase-auth")
    api("com.google.firebase:firebase-perf")

    api("com.squareup.retrofit2:retrofit:2.9.0")
    api("com.squareup.retrofit2:converter-jackson:2.9.0")
    api("androidx.preference:preference:1.1.1")

    api("androidx.security:security-crypto:1.1.0-alpha03")

    api("androidx.camera:camera-camera2:1.1.0-alpha12")
    api("androidx.camera:camera-lifecycle:1.1.0-alpha12")
    api("androidx.camera:camera-view:1.0.0-alpha32")

    api("com.github.kenglxn.QRGen:android:2.6.0")

    api("com.github.chrisbanes:PhotoView:2.3.0")
    api("androidx.work:work-runtime:2.7.1")
    api("cat.ereza:customactivityoncrash:2.3.0")

//    api("net.yslibrary.keyboardvisibilityevent:keyboardvisibilityevent:3.0.0-RC3")
//    api("com.pranavpandey.android:dynamic-toasts:3.3.1")

    implementation("androidx.hilt:hilt-work:1.0.0")
    implementation("androidx.hilt:hilt-lifecycle-viewmodel:1.0.0-alpha03")
    annotationProcessor("androidx.hilt:hilt-compiler:1.0.0")

    implementation("com.google.dagger:hilt-android:2.40.5")
    annotationProcessor("com.google.dagger:hilt-android-compiler:2.40.5")
    addDaggerAll("2.40.5")

    addGuava()

    addJetbrainsAnnotations()
    addAndroidAnnotations()

    addRoomAll()
    addPaging()

    addEspressoAll()

    addJUnitTestExtToAndroidTests()

    addJunit4ToAllTests()
    addHamcrestToAllTests()
}