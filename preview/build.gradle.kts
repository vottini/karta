import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvm("desktop") {
        compilations.configureEach {
            compilerOptions.configure {
                jvmTarget.set(JvmTarget.JVM_11)
            }
        }
    }

    sourceSets {
        getByName("desktopMain").dependencies {
            implementation(project(":karta"))
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
        }
    }
}

compose.desktop {
    application {
        mainClass = "systems.untangle.karta.preview.MainKt"
    }
}
