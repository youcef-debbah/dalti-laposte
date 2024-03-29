plugins {
    `maven-publish`
    id("com.android.library")
//    id("dz.jsoftware95.silverdocs") version "0.5.1"
//    id("dz.jsoftware95.silvercleaner-android") version "0.5.0"
//    id("net.ltgt.errorprone") version "1.3.0"
    id("com.github.ben-manes.versions")
    id("dz.jsoftware95.common-dependencies-android")
}

println("silverbox library configuration...")
project.group = "dz.jsoftware95"
project.version = "0.2." + System.currentTimeMillis()

android {
    compileSdk = 31

    buildFeatures.dataBinding = true

    defaultConfig {
        minSdk = 21
        targetSdk = 31
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
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

        }

        named("release") {
            ndk {
                debugSymbolLevel = "FULL"
            }
            isMinifyEnabled = true
            proguardFiles(defaultProguardFile, proguardRules)
        }
    }
}

repositories {
    mavenLocal()
    google()
    mavenCentral()
}

dependencies {
    implementation("dz.jsoftware95:common:+")
    implementation("dz.jsoftware95:cleaningtools:0.8.2")

//    implementation("com.google.errorprone:error_prone_core:2.5.1")
    implementation("com.google.code.findbugs:jsr305:3.0.2")
    implementation("com.google.android.gms:play-services-base:18.0.1")

    implementation("androidx.lifecycle:lifecycle-service:2.3.1")
    implementation("androidx.lifecycle:lifecycle-common:2.4.0")

    addJetbrainsAnnotations()
    addAndroidAnnotations()
    addGuava()

    addPaging()
    addRoomCore()
    addDaggerCore("2.40.5")
    addDaggerAndroidSupport("2.40.5")

    addJunit4ToAllTests()
    addMockitoToAllTests()
    addHamcrestToAllTests()
    addRobolectricToTests()

    addJUnitTestExtToAndroidTests()
}

tasks.withType(Test::class) {
    testLogging {
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }

//    filter {
//        includeTestsMatching("dz.jsoftware95.silverbox.android.concurrent.*")
//    }
}

val androidSourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    this.from(android.sourceSets["main"].java.srcDirs)
}

//silverDocs {
//    javadoc {
//        val regex = Regex(".+?\\b(concurrent|observers|common)\\b.+?")
//        source = source.filter { file -> file.path.matches(regex) }.asFileTree
//    }
//}

afterEvaluate {
    publishing {
        publications {
            this.create<MavenPublication>("debug") {
                this.from(components["debug"])
                this.artifact(androidSourcesJar)
            }
        }
    }
}