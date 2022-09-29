package raiffeisen.sbp.sdk

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URL

class BanksRepository(context: Context) {

    private val bankRedirectCountPrefs = context.getSharedPreferences(
        "bankRedirectCount",
        Context.MODE_PRIVATE
    )

    private val recentBanksState = MutableStateFlow(emptyList<BankAppInfo>())
    private val allBanksState = MutableStateFlow(emptyList<BankAppInfo>())
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        coroutineScope.launch {
            allBanksState.value = fetchBanks()
        }

        allBanksState.onEach { banks ->
            recentBanksState.value = banks.filter {
                bankRedirectCountPrefs.getInt(it.packageName, 0) != 0
            }.sortedByDescending {
                bankRedirectCountPrefs.getInt(it.packageName, 0)
            }.take(4)
        }.launchIn(coroutineScope)
    }

    fun saveBankRedirected(bankAppInfo: BankAppInfo) {
        bankRedirectCountPrefs.edit {
            putInt(
                bankAppInfo.packageName,
                bankRedirectCountPrefs.getInt(bankAppInfo.packageName, 0) + 1
            )
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
