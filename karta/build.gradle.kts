
import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.android.kotlin.multiplatform.library)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.maven.publish)
}

repositories {
  mavenCentral()
  google()
}

kotlin {
  jvm("desktop")

  androidLibrary {
    namespace = "systems.untangle.karta"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    minSdk = libs.versions.android.minSdk.get().toInt()

    compilations.configureEach {
      compilerOptions.configure {
        jvmTarget.set(JvmTarget.JVM_11)
      }
    }
  }

  sourceSets {
    commonMain.dependencies {
      implementation(compose.runtime)
      implementation(compose.foundation)
      implementation(compose.material)
      implementation(compose.ui)
      implementation(compose.components.resources)
      implementation(libs.coil)
      implementation(libs.coil.compose)
      implementation(libs.slf4f)
    }

    getByName("androidMain").dependencies {
      implementation(libs.androidx.activity.compose)
      implementation(compose.preview)
    }

    getByName("desktopMain").dependencies {
      implementation(compose.desktop.currentOs)
      implementation(libs.kotlinx.coroutines.swing)
    }
  }
}

compose.resources {
  publicResClass = true
  packageOfResClass = "systems.untangle.karta.resources"
  generateResClass = always
}

configurations.all {
  resolutionStrategy {
    force("androidx.core:core:1.13.1")
    force("androidx.core:core-ktx:1.13.1")
  }
}

group = "systems.untangle"
version = "0.1.3"
