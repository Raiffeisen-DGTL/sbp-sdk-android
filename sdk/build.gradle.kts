import org.json.JSONObject
import java.net.URL

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
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

tasks.create("raiffeisenSbpSdkPreload") {
    group = "build"

    val response = URL("https://qr.nspk.ru/proxyapp/c2bmembers.json").readText()
    val json = JSONObject(response)
    val banks = json.getJSONArray("dictionary")

    val classText = buildString {
        append("package raiffeisen.sbp.sdk")
        appendLine()
        appendLine()
        append("object PreloadedBanks {")
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

tasks.preBuild {
    dependsOn("raiffeisenSbpSdkPreload")
}