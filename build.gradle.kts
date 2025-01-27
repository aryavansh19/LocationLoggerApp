// Project-level build.gradle.kts

plugins {
    alias(libs.plugins.android.application) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}

buildscript {
    repositories {
        google()  // Essential for Firebase and other Google libraries
        mavenCentral()  // For other dependencies
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.0.4")  // Android Gradle Plugin version
        classpath("com.google.gms:google-services:4.4.2")  // Firebase Plugin for Google services
    }
}
