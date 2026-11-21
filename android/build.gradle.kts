import io.github.rwx.build.AppVersionSupport
import io.github.rwx.build.AssetListGenerationSupport

plugins {
    alias(libs.plugins.android.application)
}

val assetListGeneration = AssetListGenerationSupport.register(project)

val releaseVersionCode = AppVersionSupport.versionCode(project.version.toString())

fun signingValue(environmentName: String, propertyName: String) =
    providers.gradleProperty(propertyName).orElse(providers.environmentVariable(environmentName))

val releaseKeystoreFile = signingValue("KEYSTORE_FILE", "androidKeystoreFile")
val releaseKeystorePassword = signingValue("KEYSTORE_PASSWORD", "androidKeystorePassword")
val releaseKeyAlias = signingValue("KEY_ALIAS", "androidKeyAlias")
val releaseKeyPassword = signingValue("KEY_PASSWORD", "androidKeyPassword")
val signingValues = listOf(
    releaseKeystoreFile,
    releaseKeystorePassword,
    releaseKeyAlias,
    releaseKeyPassword,
)
val releaseSigningConfigured = signingValues.all { it.isPresent }

kotlin {
    jvmToolchain(25)
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25)
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":ui"))
    implementation(libs.android.webrtc)
    implementation(libs.koin.android)
    implementation(libs.activity.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.timber)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    testImplementation(kotlin("test-junit5"))
    testImplementation(libs.robolectric)
    testImplementation(libs.junit)
    testRuntimeOnly(libs.junit.vintage.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}

android {
    namespace = project.group.toString()
    compileSdk = 37
    useLibrary("org.apache.http.legacy")
    testOptions.unitTests.isIncludeAndroidResources = true

    defaultConfig {
        applicationId = project.group.toString()
        minSdk = 28
        //noinspection EditedTargetSdkVersion
        targetSdk = 37
        versionCode = releaseVersionCode
        versionName = project.version.toString()
        ndk {
            //noinspection ChromeOsAbiSupport
            abiFilters += listOf("arm64-v8a")
        }
    }

    signingConfigs {
        if (releaseSigningConfigured) {
            create("release") {
                storeFile = file(releaseKeystoreFile.get())
                storePassword = releaseKeystorePassword.get()
                keyAlias = releaseKeyAlias.get()
                keyPassword = releaseKeyPassword.get()
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (releaseSigningConfigured) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    sourceSets {
        named("main") {
            manifest.srcFile("src/main/AndroidManifest.xml")
            assets.directories.add("../assets")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
        isCoreLibraryDesugaringEnabled = true
    }

    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
        resources {
            excludes += listOf(
                "META-INF/**"
            )
        }
    }
}

tasks.matching { task -> task.name.startsWith("merge") && task.name.endsWith("Assets") }
    .configureEach {
        dependsOn(assetListGeneration.task)
    }

tasks.matching { task -> task.name.contains("lintVital", ignoreCase = true) }
    .configureEach {
        dependsOn(assetListGeneration.task)
    }

// WORKAROUND (CMP-9547): with AGP 9.x + com.android.kotlin.multiplatform.library,
// :ui composeResources are not published as assets automatically. Register the
// workaround copy output (see ui/build.gradle.kts) as a static asset source and
// order it before asset merging. Remove once upstream fixes it.
androidComponents {
    onVariants(selector().all()) { variant ->
        val composeResourcesAssetsDir = rootProject.file(
            "ui/build/generated/compose/resourceGenerator/androidMain/assets"
        )
        variant.sources.assets?.addStaticSourceDirectory(composeResourcesAssetsDir.absolutePath)
    }
}

afterEvaluate {
    val composeResourceTasks = listOf(
        ":ui:copyAndroidMainComposeResourcesToAndroidAssets",
        ":ui:prepareComposeResourcesTaskForCommonMain",
        ":ui:convertXmlValueResourcesForCommonMain",
        ":ui:copyNonXmlValueResourcesForCommonMain"
    )

    tasks.matching { task ->
        task.name.contains("merge") && task.name.contains("Assets")
    }.configureEach {
        composeResourceTasks.forEach { taskPath ->
            dependsOn(taskPath)
        }
    }

    tasks.matching { task ->
        task.name.contains("lintVital", ignoreCase = true)
    }.configureEach {
        composeResourceTasks.forEach { taskPath ->
            dependsOn(taskPath)
        }
    }
}
