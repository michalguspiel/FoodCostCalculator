import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.detekt)
    alias(libs.plugins.compose.compiler)
}

android {
    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localProperties.load(localPropertiesFile.inputStream())
    } else {
        println("local.properties file not found, skipping loading properties.")
    }

    namespace = "com.erdees.foodcostcalc"

    compileSdk = 36

    defaultConfig {
        applicationId = "com.erdees.foodcostcalc"
        minSdk = 26
        targetSdk = 35
        versionCode = project.property("version_code").toString().toInt()
        versionName = project.property("version_name").toString()
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            debugSymbolLevel = "FULL"
        }
    }

    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/notice.txt",
                "META-INF/ASL2.0"
            )
        }
    }

    val keystoreFile = file("demo.keystore")
    signingConfigs {
        create("demo") {
            keyAlias = localProperties.getProperty("keyAlias")
            keyPassword = localProperties.getProperty("keyPassword")
            storePassword = localProperties.getProperty("storePassword")
            storeFile = keystoreFile
        }
    }

    buildTypes {

        getByName("debug") {
            manifestPlaceholders["crashlyticsCollectionEnabled"] = false
        }

        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            manifestPlaceholders["crashlyticsCollectionEnabled"] = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        create("demo") {
            initWith(getByName("release"))
            signingConfig = if (keystoreFile.exists()) signingConfigs.getByName("demo") else signingConfigs.getByName("debug")
        }
        create("signedDebug") {
            initWith(getByName("debug"))
            signingConfig = if (keystoreFile.exists()) signingConfigs.getByName("demo") else signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    configurations.all {
        exclude(group = "org.apache.httpcomponents", module = "httpclient")
    }

    room {
        schemaDirectory("$projectDir/schemas")
    }

    sourceSets {
        getByName("androidTest") {
            assets.srcDirs(file("$projectDir/schemas"))
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

detekt {
    toolVersion = libs.versions.detekt.get()
    config.setFrom(rootDir.resolve("config/detekt/config.yml"))
    baseline = file(rootDir.resolve("config/detekt/baseline.yml"))
    disableDefaultRuleSets = false
    debug = false
    ignoredBuildTypes = listOf("release")
    basePath = projectDir.toString()
}

dependencies {
    // Compose BOM
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Compose libraries
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.core)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.activity.compose)
    implementation(libs.navigation.compose)
    implementation(libs.compose.ui.viewbinding)
    implementation(libs.coil.compose)

    // Android Studio Preview support
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)

    // UI Tests
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)

    // Basic Android libraries
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.core.ktx)

    // KotlinX
    implementation(libs.coroutines.android)
    implementation(libs.kotlinx.immutable.collections)

    // Lifecycle
    implementation(libs.lifecycle.viewmodel.ktx)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    androidTestImplementation(libs.room.testing)
    ksp(libs.room.compiler)

    // Activity KTX
    implementation(libs.activity.ktx)

    // Play Store Review
    implementation(libs.play.review.ktx)

    // Splashscreen
    implementation(libs.core.splashscreen)

    // Google Drive API
    implementation(libs.play.services.drive)
    implementation(libs.play.services.auth)
    implementation(libs.google.http.client.gson)
    implementation(libs.google.api.client.android)
    implementation(libs.google.api.services.drive)

    // Serialization
    implementation(libs.serialization.json)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.appcheck)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.firestore)

    // Ads
    implementation(libs.play.services.ads)

    // Guava (conflict workaround)
    implementation(libs.guava.listenablefuture)

    // Billing
    implementation(libs.billing.ktx)

    // Confetti (Konfetti)
    implementation(libs.konfetti.compose)

    // Koin (DI)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    // Timber
    implementation(libs.timber)

    // DataStore
    implementation(libs.androidx.datastore)

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    testImplementation(libs.mockk)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.coroutines.test)

    // Detekt
    detektPlugins(libs.detekt.formatting)
    detektPlugins(libs.compose.rules.detekt)

    // Canary Leak
    debugImplementation(libs.canary.leak)
}
