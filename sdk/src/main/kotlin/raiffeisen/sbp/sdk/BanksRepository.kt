package raiffeisen.sbp.sdk

import kotlinx.coroutines.flow.flow

class BanksRepository {
    fun recentBanksFlow() = flow {
        emit(
            listOf(
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
            )
        )
    }

    fun allBanksFlow() = flow {
        emit(
            listOf(
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
    }
}
