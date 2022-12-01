import org.gradle.jvm.tasks.Jar
import org.json.JSONObject
import java.net.URL

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("org.gradle.maven-publish")
    id("signing")
}

val codegenDirPath = "${buildDir}/generated/source/raiffeisenSbpSdk/main"
val codegenPackagePath = "$codegenDirPath/raiffeisen/sbp/sdk/android"

android {
    namespace = "raiffeisen.sbp.sdk.android"
    compileSdk = 33

    defaultConfig {
        minSdk = 21
        targetSdk = 33
    }

    sourceSets {
        getByName("main") {
            java {
                srcDir(codegenDirPath)
            }
        }
    }

    publishing {
        multipleVariants {
            singleVariant("release") {
                withSourcesJar()
            }
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.5.1")
    implementation("com.google.android.material:material:1.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
    implementation("com.github.bumptech.glide:glide:4.13.2")
    kapt("com.github.bumptech.glide:compiler:4.13.2")
}

ext["signing.keyId"] = null
ext["signing.password"] = null
ext["signing.secretKeyRingFile"] = null
ext["ossrhUsername"] = null
ext["ossrhPassword"] = null

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Raiffeisen-DGTL/sbp-sdk-android")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }

    publications.create<MavenPublication>("release") {
        version = "1.0.1"
        group = "ru.raiffeisen"
        artifactId = "sbp-sdk-android"
        artifact(javadocJar.get())
        pom {
            name.set("sbp-sdk-android")
            description.set("Android SDK платежной формы СБП")
            url.set("https://github.com/Raiffeisen-DGTL/sbp-sdk-android")

            licenses {
                license {
                    name.set("MIT")
                    url.set("https://github.com/Raiffeisen-DGTL/sbp-sdk-android/LICENSE.md")
                }
            }
            developers {
                developer {
                    id.set("epicarchitect")
                    name.set("Alexander Kolmachikhin")
                    email.set("akolmachikhin@icerockdev.com")
                }
            }
            scm {
                url.set("https://github.com/Raiffeisen-DGTL/sbp-sdk-android")
            }
        }

        afterEvaluate {
            from(components["release"])
        }
    }
}

tasks.create("raiffeisenSbpSdkPreload") {
    group = "build"

    doFirst {
        val response = URL("https://qr.nspk.ru/proxyapp/c2bmembers.json").readText()
        val json = JSONObject(response)
        val banks = json.getJSONArray("dictionary")

        val classText = buildString {
            append("package raiffeisen.sbp.sdk.android")
            appendLine()
            appendLine()
            append("internal object PreloadedBanks {")
            appendLine()
            append("\tval banks = listOf(")
            repeat(banks.length()) {
                val bank = banks.getJSONObject(it)
                appendLine()
                append("\t\tBankAppInfo(")
                appendLine()
                append("\t\t\tname = \"${bank.getString("bankName")}\",")
                appendLine()
                append("\t\t\tlogoUrl = \"${bank.getString("logoURL")}\",")
                appendLine()
                append("\t\t\tschema = \"${bank.getString("schema")}\",")
                appendLine()
                append(
                    "\t\t\tpackageName = ${
                        if (bank.has("package_name")) "\"${bank.getString("package_name")}\""
                        else "null"
                    },"
                )
                appendLine()
                append("\t\t),")
            }
            appendLine()
            append("\t)")
            appendLine()
            append("}")
        }

        layout.buildDirectory.dir(codegenPackagePath).get().asFile.mkdirs()
        val file = layout.buildDirectory.dir("$codegenPackagePath/PreloadedBanks.kt").get().asFile
        file.delete()
        file.createNewFile()
        file.writeText(classText)
    }
}

tasks.preBuild {
    dependsOn("raiffeisenSbpSdkPreload")
}