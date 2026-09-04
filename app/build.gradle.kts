import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

// Τα στοιχεία υπογραφής (passwords, alias) ΔΕΝ μπαίνουν ποτέ απευθείας εδώ, γιατί το
// build.gradle.kts ανεβαίνει στο git — μπαίνουν σε ξεχωριστό keystore.properties στη ρίζα
// του project, το οποίο είναι στο .gitignore και υπάρχει μόνο τοπικά σε κάθε μηχάνημα.
// Αν το αρχείο λείπει (π.χ. σε μηχάνημα άλλου συνεργάτη που δεν έχει το keystore), δεν
// πετάμε σφάλμα εδώ — απλά το signing config του release παραμένει άδειο/μη λειτουργικό,
// ώστε τα debug builds να συνεχίζουν να δουλεύουν κανονικά.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    namespace = "com.foxnks.xeiristisexamquiz"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.foxnks.xeiristisexamquiz"
        minSdk = 24
        targetSdk = 37
        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Το signing config διαβάζει τα στοιχεία από το keystoreProperties object παραπάνω,
    // ΜΟΝΟ αν το keystore.properties υπήρχε — αλλιώς μένουν όλα null και το release build
    // απλά δεν θα είναι υπογεγραμμένο (δεν σκάει το build λόγω λείπον αρχείο).
    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }

    buildTypes {
        release {
            // Έτσι κάθε release build (bundleRelease ή ο οδηγός "Generate Signed Bundle")
            // υπογράφεται αυτόματα, χωρίς να χρειάζεται να ξαναδίνεις τα στοιχεία κάθε φορά.
            signingConfig = signingConfigs.getByName("release")
            optimization {
                enable = true
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}