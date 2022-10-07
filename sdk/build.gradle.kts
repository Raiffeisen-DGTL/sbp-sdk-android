import org.gradle.jvm.tasks.Jar
import org.json.JSONObject
import java.net.URL
import java.util.Properties

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("org.gradle.maven-publish")
    id("signing")
}

val codegenDirPath = "${buildDir}/generated/source/raiffeisenSbpSdk/main"
val codegenPackagePath = "$codegenDirPath/raiffeisen/sbp/sdk"

android {
    namespace = "raiffeisen.sbp.sdk"
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

val secretPropsFile = project.rootProject.file("local.properties")
if (secretPropsFile.exists()) {
    secretPropsFile.reader().use {
        Properties().apply {
            load(it)
        }
    }.onEach { (name, value) ->
        ext[name.toString()] = value
    }
} else {
    ext["signing.keyId"] = System.getenv("SIGNING_KEY_ID")
    ext["signing.password"] = System.getenv("SIGNING_PASSWORD")
    ext["signing.secretKeyRingFile"] = System.getenv("SIGNING_SECRET_KEY_RING_FILE")
    ext["ossrhUsername"] = System.getenv("OSSRH_USERNAME")
    ext["ossrhPassword"] = System.getenv("OSSRH_PASSWORD")
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

fun getExtraString(name: String) = ext[name]?.toString()

publishing {
    repositories {
        maven {
            name = "sonatype"
            setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = getExtraString("ossrhUsername")
                password = getExtraString("ossrhPassword")
            }
        }
    }

    publications.register<MavenPublication>("release") {
        version = "1.0.0"
        group = "ru.raiffeisen"
        artifactId = "sbp-sdk"
        artifact(javadocJar.get())
        pom {
            name.set("ecom-sdk-android")
            description.set("Android SDK платежной формы СБП")
            url.set("https://github.com/Raiffeisen-DGTL/ecom-sdk-android")

            licenses {
                license {
                    name.set("MIT")
                    url.set("https://github.com/Raiffeisen-DGTL/ecom-sdk-android/LICENSE.md")
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
                url.set("https://github.com/Raiffeisen-DGTL/ecom-sdk-android")
            }
        }

        afterEvaluate {
            from(components["release"])
        }
    }
}

signing {
    if (getExtraString("signing.keyId") != null) {
        sign(publishing.publications)
    }
}

tasks.create("raiffeisenSbpSdkPreload") {
    group = "build"

    doFirst {
        val response = URL("https://qr.nspk.ru/proxyapp/c2bmembers.json").readText()
        val json = JSONObject(response)
        val banks = json.getJSONArray("dictionary")

        val classText = buildString {
            append("package raiffeisen.sbp.sdk")
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