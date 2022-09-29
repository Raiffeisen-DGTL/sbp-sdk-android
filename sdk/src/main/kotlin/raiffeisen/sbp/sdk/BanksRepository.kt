package raiffeisen.sbp.sdk

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URL

class BanksRepository {

    private val recentBanksState = MutableStateFlow(emptyList<BankAppInfo>())
    private val allBanksState = MutableStateFlow(emptyList<BankAppInfo>())

    init {
        CoroutineScope(Dispatchers.IO).launch {
            val response = fetchBanks()
            allBanksState.value = response
        }
    }

    fun recentBanksFlow(): Flow<List<BankAppInfo>> = recentBanksState

    fun allBanksFlow(): Flow<List<BankAppInfo>> = allBanksState

    private fun fetchBanks(): List<BankAppInfo> {
        val response = URL(BANKS_INFO_URL).readText()
        val json = JSONObject(response)
        val jsonBanks = json.getJSONArray("dictionary")
        return List(jsonBanks.length()) { index ->
            val jsonBank = jsonBanks.getJSONObject(index)
            BankAppInfo(
                name = jsonBank.getString("bankName"),
                logoUrl = jsonBank.getString("logoURL"),
                schema = jsonBank.getString("schema"),
                packageName = if (jsonBank.has("package_name"))
                    jsonBank.getString("package_name") else null
            )
        }
    }

    companion object {
        private const val BANKS_INFO_URL = "https://qr.nspk.ru/proxyapp/c2bmembers.json"
    }
}
