
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.android.kotlin.multiplatform.library)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.maven.publish)
  signing
}

repositories {
  mavenCentral()
  google()
}

kotlin {
  jvm("desktop")

  android {
    namespace = "systems.untangle.karta"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    minSdk = libs.versions.android.minSdk.get().toInt()

    optimization {
      consumerKeepRules.files("consumer-rules.pro")
    }

    compilations.configureEach {
      compileTaskProvider.configure {
        compilerOptions {
          jvmTarget.set(JvmTarget.JVM_11)
        }
      }
    }
  }

  sourceSets {
    commonMain.dependencies {
      implementation(libs.compose.runtime)
      implementation(libs.compose.foundation)
      implementation(libs.compose.material)
      implementation(libs.compose.ui)
      implementation(libs.compose.components.resources)
      implementation(libs.coil)
      implementation(libs.coil.compose)
    }

    getByName("androidMain").dependencies {
      implementation(libs.slf4f)
      implementation(libs.androidx.activity.compose)
      implementation(libs.compose.ui.tooling.preview)
    }

    getByName("desktopMain").dependencies {
      implementation(compose.desktop.currentOs)
      implementation(libs.kotlinx.coroutines.swing)
      implementation(libs.sqlite.jdbc)
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
version = "0.5.5"

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates(
        group.toString(),
        "karta",
        version.toString()
    )

    pom {
        name = "Karta"
        description = "A tile map component in pure compose multiplatform"
        inceptionYear = "2025"
        url = "https://github.com/votini/karta"

        licenses {
            license {
                name = "MIT License"
                url = "http://www.opensource.org/licenses/mit-license.php"
            }
        }

        developers {
            developer {
                name = "Gustavo Venturini"
                email = "gustavo.c.venturini@gmail.com"
            }
        }

        scm {
            url = "https://github.com/votini/karta"
            connection = "scm:git:git://github.com:vottini/karta.git"
            developerConnection = "scm:git:ssh://github.com:vottini/karta.git"
        }
    }
}

