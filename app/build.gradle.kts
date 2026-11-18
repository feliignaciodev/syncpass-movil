

import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    jacoco
}

android {
    namespace = "com.example.syncpassapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.syncpassapp"
        minSdk = 27
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8080/\"")
        buildConfigField("String", "NTFY_BASE_URL", "\"https://ntfy.sh\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "API_BASE_URL", "\"https://syncpass.duckdns.org/\"")
        }
        debug {
            buildConfigField("String", "API_BASE_URL", "\"https://syncpass.duckdns.org/\"")
            // Genera build/outputs/jacoco/testDebugUnitTest.exec para los tests JVM.
            enableUnitTestCoverage = true
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
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.zxing.core)

    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.mlkit.barcode.scanning)

    // Room (Local SQLite)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Retrofit & OkHttp (Networking / API)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    // Hilt (Dependency Injection)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // DataStore (Tokens)
    implementation(libs.datastore.preferences)

    // JWT Decode
    implementation(libs.auth0.jwt)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

// ---------------------------------------------------------------------------
// JaCoCo para los tests JVM (testDebugUnitTest).
// Solo se excluye código GENERADO por Hilt/Room/KSP, BuildConfig, R/BR y
// databinding. La lógica propia NO se excluye para no inflar la cobertura.
// ---------------------------------------------------------------------------

jacoco {
    toolVersion = "0.8.12"
}

val clasesGeneradasExcluidas = listOf(
    "**/R.class",
    "**/R$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "**/BR.class",
    "**/databinding/**",
    "**/*_Impl.class",
    "**/*_Impl$*.class",
    "**/*_Factory.class",
    "**/*_Factory$*.class",
    "**/*_MembersInjector.class",
    "**/*_MembersInjector$*.class",
    "**/Dagger*.class",
    "**/Dagger*$*.class",
    "**/*_Component.class",
    "**/*_Component$*.class",
    "**/*Module_*.class",
    "**/Hilt_*.class",
    "**/*_Hilt*.class",
    "**/*_Delegate.class",
    "**/*_ViewProvider.class"
)

val raizBuildCobertura = layout.buildDirectory.get().asFile
val fuentesCobertura = files("src/main/java", "src/main/kotlin")
val clasesCobertura = files(
    fileTree("$raizBuildCobertura/tmp/kotlin-classes/debug") {
        include("**/*.class")
        exclude(clasesGeneradasExcluidas)
    },
    fileTree("$raizBuildCobertura/intermediates/javac/debug/classes") {
        include("**/*.class")
        exclude(clasesGeneradasExcluidas)
    }
)
val ejecucionCobertura = fileTree(raizBuildCobertura) {
    include("outputs/jacoco/**/*.exec")
    include("jacoco/**/*.exec")
}

val jacocoTestReport = tasks.register<JacocoReport>("jacocoTestReport") {
    group = "reporting"
    description = "Genera el reporte HTML/XML de cobertura de los tests JVM (debug)."
    dependsOn("testDebugUnitTest")
    classDirectories.setFrom(clasesCobertura)
    sourceDirectories.setFrom(fuentesCobertura)
    executionData.setFrom(ejecucionCobertura)
    reports {
        html.required.set(true)
        xml.required.set(true)
        csv.required.set(false)
    }
}

val jacocoTestCoverageVerification = tasks.register<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    group = "verification"
    description = "Verifica que la cobertura JVM sea al menos 30%."
    dependsOn(jacocoTestReport)
    classDirectories.setFrom(clasesCobertura)
    sourceDirectories.setFrom(fuentesCobertura)
    executionData.setFrom(ejecucionCobertura)
    violationRules {
        rule {
            limit {
                minimum = BigDecimal("0.30")
            }
        }
    }
}



