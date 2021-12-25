rootProject.name = "Dalti-laposte"

include(":client")
include(":admin")
include(":core")
include(":silverbox")

pluginManagement {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}