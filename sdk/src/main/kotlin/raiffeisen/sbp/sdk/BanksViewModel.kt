package raiffeisen.sbp.sdk

import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update


class BanksViewModel(
    private val banksRepository: BanksRepository,
    private val packageManager: PackageManager
) : ViewModel() {

    private val searchState = MutableStateFlow("")

    val state = combine(
        banksRepository.recentBanksFlow(),
        banksRepository.allBanksFlow(),
        searchState
    ) { recentBanks, allBanks, searchText ->
        val filteredRecentBanks = recentBanks.filter {
            isPackageInstalled(it.packageName ?: "")
        }.ifEmpty {
            recentBanks
        }.let {
            if (searchText.isEmpty()) it
            else it.filterByName(searchText)
        }

        val filteredAllBanks = allBanks.filter {
            isPackageInstalled(it.packageName ?: "")
        }.ifEmpty {
            allBanks
        }.let {
            if (searchText.isEmpty()) it
            else it.filterByName(searchText)
        }

        State(
            searchText = searchText,
            recentBanks = filteredRecentBanks,
            allBanks = filteredAllBanks
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        State(
            searchText = "",
            recentBanks = emptyList(),
            allBanks = emptyList()
        )
    )

    fun setSearchText(text: String) {
        searchState.update { text }
    }

    fun saveBankRedirected(bankAppInfo: BankAppInfo) {
        banksRepository.saveBankRedirected(bankAppInfo)
    }

    private fun List<BankAppInfo>.filterByName(
        text: String
    ) = text.trim().split(" ").let { textParts ->
        filter { info ->
            textParts.all {
                info.name.contains(
                    other = it,
                    ignoreCase = true
                )
            }
        }
    }

    private fun isPackageInstalled(packageName: String) = try {
        packageManager.getPackageGids(packageName)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }

    data class State(
        val searchText: String,
        val recentBanks: List<BankAppInfo>,
        val allBanks: List<BankAppInfo>
    )
}