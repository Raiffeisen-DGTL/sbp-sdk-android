package raiffeisen.sbp.sdk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class BanksViewModel(private val banksRepository: BanksRepository) : ViewModel() {

    private val searchState = MutableStateFlow("")

    val state = combine(
        banksRepository.recentBanksFlow(),
        banksRepository.allBanksFlow(),
        searchState
    ) { recentBanks, allBanks, searchText ->
        val filteredRecentBanks = recentBanks.let {
            if (searchText.isEmpty()) it
            else it.filterByName(searchText)
        }

        val filteredAllBanks = allBanks.let {
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

    data class State(
        val searchText: String,
        val recentBanks: List<BankAppInfo>,
        val allBanks: List<BankAppInfo>,
    )
}

private val fakeState = BanksViewModel.State(
    searchText = "",
    recentBanks = listOf(
        BankAppInfo(
            name = "asdasdasdad",
            logoUrl = "https://qr.nspk.ru/proxyapp/logo/bank100000000111.png",
            schema = "",
            packageName = ""
        ),
        BankAppInfo(
            name = "Sberbank",
            logoUrl = "https://qr.nspk.ru/proxyapp/logo/bank100000000111.png",
            schema = "",
            packageName = ""
        ),
        BankAppInfo(
            name = "Tinkofff",
            logoUrl = "https://qr.nspk.ru/proxyapp/logo/bank100000000111.png",
            schema = "",
            packageName = ""
        )
    ),
    allBanks = listOf(
        BankAppInfo(
            name = "asdasdasdasd",
            logoUrl = "https://qr.nspk.ru/proxyapp/logo/bank100000000111.png",
            schema = "",
            packageName = ""
        ),
        BankAppInfo(
            name = "asasdasdasdasdasdd",
            logoUrl = "https://qr.nspk.ru/proxyapp/logo/bank100000000111.png",
            schema = "",
            packageName = ""
        ),
        BankAppInfo(
            name = "asdasdasdasd",
            logoUrl = "https://qr.nspk.ru/proxyapp/logo/bank100000000111.png",
            schema = "",
            packageName = ""
        ),
        BankAppInfo(
            name = "asdasdasdasdasd",
            logoUrl = "https://qr.nspk.ru/proxyapp/logo/bank100000000111.png",
            schema = "",
            packageName = ""
        ),
        BankAppInfo(
            name = "asd",
            logoUrl = "https://qr.nspk.ru/proxyapp/logo/bank100000000111.png",
            schema = "",
            packageName = ""
        ),
        BankAppInfo(
            name = "asd",
            logoUrl = "https://qr.nspk.ru/proxyapp/logo/bank100000000111.png",
            schema = "",
            packageName = ""
        ),
        BankAppInfo(
            name = "asd",
            logoUrl = "https://qr.nspk.ru/proxyapp/logo/bank100000000111.png",
            schema = "",
            packageName = ""
        ),
        BankAppInfo(
            name = "asd",
            logoUrl = "https://qr.nspk.ru/proxyapp/logo/bank100000000111.png",
            schema = "",
            packageName = ""
        ),
        BankAppInfo(
            name = "asd",
            logoUrl = "https://qr.nspk.ru/proxyapp/logo/bank100000000111.png",
            schema = "",
            packageName = ""
        ),
        BankAppInfo(
            name = "asd",
            logoUrl = "https://qr.nspk.ru/proxyapp/logo/bank100000000111.png",
            schema = "",
            packageName = ""
        )
    )
)