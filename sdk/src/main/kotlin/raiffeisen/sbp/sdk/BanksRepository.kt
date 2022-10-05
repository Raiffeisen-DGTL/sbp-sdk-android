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
        BANK_REDIRECT_COUNT_PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val recentBanksState = MutableStateFlow(emptyList<BankAppInfo>())
    private val allBanksState = MutableStateFlow(PreloadedBanks.banks)
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        coroutineScope.launch {
            try {
                allBanksState.value = fetchBanks()
            } catch (e: Exception) {
                allBanksState.value = PreloadedBanks.banks
            }
        }

        allBanksState.onEach { banks ->
            recentBanksState.value = banks.filter {
                bankRedirectCountPrefs.getInt(it.packageName, 0) != 0
            }.sortedByDescending {
                bankRedirectCountPrefs.getInt(it.packageName, 0)
            }.take(MAX_RECENT_BANKS)
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
        val jsonBanks = json.getJSONArray(JSON_ARRAY_DICTIONARY)
        return List(jsonBanks.length()) { index ->
            val jsonBank = jsonBanks.getJSONObject(index)
            BankAppInfo(
                name = jsonBank.getString(JSON_STRING_BANK_NAME),
                logoUrl = jsonBank.getString(JSON_STRING_LOGO_URL),
                schema = jsonBank.getString(JSON_STRING_SCHEMA),
                packageName = if (jsonBank.has(JSON_STRING_PACKAGE_NAME))
                    jsonBank.getString(JSON_STRING_PACKAGE_NAME) else null
            )
        }
    }

    companion object {
        private const val BANK_REDIRECT_COUNT_PREFS_NAME = "bankRedirectCount"
        private const val BANKS_INFO_URL = "https://qr.nspk.ru/proxyapp/c2bmembers.json"

        private const val MAX_RECENT_BANKS = 4

        private const val JSON_ARRAY_DICTIONARY = "dictionary"
        private const val JSON_STRING_BANK_NAME = "bankName"
        private const val JSON_STRING_LOGO_URL = "logoURL"
        private const val JSON_STRING_SCHEMA = "schema"
        private const val JSON_STRING_PACKAGE_NAME = "package_name"
    }
}
