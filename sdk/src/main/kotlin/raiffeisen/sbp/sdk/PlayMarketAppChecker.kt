package raiffeisen.sbp.sdk

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import javax.net.ssl.HttpsURLConnection

internal class PlayMarketAppChecker {

    suspend fun checkPackageExists(packageName: String) = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://play.google.com/store/apps/details?id=$packageName")
            val connection = url.openConnection() as HttpsURLConnection
            val code = connection.responseCode
            connection.disconnect()
            code == 200
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}