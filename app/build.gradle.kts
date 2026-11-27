import com.android.build.api.artifact.SingleArtifact
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val unsignedRelease = providers.gradleProperty("unsignedRelease").map { it.toBoolean() }.getOrElse(false)

val releaseSigningFile = rootProject.file("keystore.properties")
val releaseSigning = Properties().apply {
    if (releaseSigningFile.isFile) {
        releaseSigningFile.reader(Charsets.UTF_8).use { load(it) }
    }
}

android {
    namespace = "de.gymmane.scanner"
    compileSdk = 36
    defaultConfig {
        applicationId = "de.gymmane.scanner"
        minSdk = 26
        targetSdk = 36
        versionCode = 11
        versionName = "2.1.0"
        buildConfigField("String", "REPOSITORY_URL", "\"${providers.gradleProperty("scanner.repositoryUrl").getOrElse("")}\"")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures { compose = true; buildConfig = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    signingConfigs {
        create("release") {
            val keyPath = releaseSigning.getProperty("storeFile").orEmpty()
            if (keyPath.isNotBlank()) storeFile = rootProject.file(keyPath)
            storePassword = releaseSigning.getProperty("storePassword").orEmpty()
            keyAlias = releaseSigning.getProperty("keyAlias").orEmpty()
            keyPassword = releaseSigning.getProperty("keyPassword").orEmpty()
        }
    }
    buildTypes {
        release {
            signingConfig = if (unsignedRelease) null else signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-dev"
        }
    }
    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    testOptions { unitTests.isReturnDefaultValues = true }
}

kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }

// Export each assembled release to the project root, retaining earlier versions.
androidComponents {
    onVariants(selector().withBuildType("release")) { variant ->
        val versionName = variant.outputs.single().versionName
        val exportApk = tasks.register<Copy>("exportReleaseApk") {
            group = "build"
            description = "Copies the release APK to releases with its version in the filename."
            from(variant.artifacts.get(SingleArtifact.APK)) {
                include("*.apk")
                rename { "GymManeScanner-${versionName.get()}-release.apk" }
            }
            into(rootProject.layout.projectDirectory.dir("releases"))
        }
        tasks.matching { it.name == "assembleRelease" }.configureEach {
            dependsOn(exportApk)
        }
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.10.01"))
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.animation:animation")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.camera:camera-camera2:1.5.1")
    implementation("androidx.camera:camera-lifecycle:1.5.1")
    implementation("androidx.camera:camera-view:1.5.1")
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("com.mikepenz:multiplatform-markdown-renderer-m3:0.38.1")
    implementation("com.mikepenz:multiplatform-markdown-renderer-coil2:0.38.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20250517")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.10.01"))
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

// Validate only release signing; debug builds do not need private credentials.
tasks.matching { it.name == "validateSigningRelease" }.configureEach {
    onlyIf { !unsignedRelease }
    doFirst {
        check(releaseSigningFile.isFile) {
            "Release signing: create keystore.properties from keystore.properties.example."
        }
        val required = listOf("storeFile", "storePassword", "keyAlias", "keyPassword")
        check(required.all { !releaseSigning.getProperty(it).isNullOrBlank() }) {
            "Release signing: fill in storeFile, storePassword, keyAlias and keyPassword in keystore.properties."
        }
        check(rootProject.file(releaseSigning.getProperty("storeFile")).isFile) {
            "Release signing: the configured keystore file does not exist."
        }
    }
}

