

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
            // La cobertura de los tests JVM la mide el plugin `jacoco` de Gradle
            // (tareas jacocoTestReport y jacocoTestCoverageVerification, al final del archivo).
            enableUnitTestCoverage = false
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
    "**/*_ViewProvider.class",
    // UI de Compose (pantallas, diálogos y tema) no es lógica de negocio testeable en JVM.
    "**/*Screen*.class",
    "**/*Screen$*.class",
    "**/feature/*/ui/*Dialog*.class",
    "**/feature/*/ui/*Dialog*$*.class",
    "**/core/designsystem/theme/*",
    "**/core/designsystem/components/*",
    "**/core/designsystem/components/*$*",
    "**/ui/theme/*"
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
    include("jacoco/testDebugUnitTest.exec")
}

jacoco {
    // Misma versión que usa AGP 8.8 por defecto.
    toolVersion = "0.8.12"
}

// El plugin `jacoco` agrega su agente a cada tarea Test y escribe
// build/jacoco/<tarea>.exec con rutas relativas al proyecto: funciona en cualquier PC.
tasks.withType<Test>().configureEach {
    extensions.configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = false
        // Clases del propio JDK: no son código de la app y JaCoCo no debe instrumentarlas
        // (con JDK recientes, como el JBR 25 de Android Studio, generan avisos).
        excludes = listOf("jdk.*", "sun.*", "java.*", "javax.*", "com.sun.*", "org.jcp.*")
    }
}

// ./gradlew jacocoTestReport
//   → app/build/reports/jacoco/jacocoTestReport/html/index.html (y .xml)
val jacocoTestReport = tasks.register<JacocoReport>("jacocoTestReport") {
    group = "verification"
    description = "Reporte de cobertura de los tests JVM (testDebugUnitTest)."
    dependsOn("testDebugUnitTest")
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
    sourceDirectories.setFrom(fuentesCobertura)
    classDirectories.setFrom(clasesCobertura)
    executionData.setFrom(ejecucionCobertura)
}

// ./gradlew jacocoTestCoverageVerification
//   → falla si la cobertura de líneas baja del 30 % (mínimo exigido por la rúbrica).
tasks.register<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    group = "verification"
    description = "Verifica la cobertura mínima de líneas (30 %)."
    dependsOn(jacocoTestReport)
    sourceDirectories.setFrom(fuentesCobertura)
    classDirectories.setFrom(clasesCobertura)
    executionData.setFrom(ejecucionCobertura)
    violationRules {
        rule {
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.30".toBigDecimal()
            }
        }
    }
}
