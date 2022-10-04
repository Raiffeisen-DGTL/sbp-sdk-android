package raiffeisen.sbp.sdk

import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager

object RaiffeisenSbpSdk {

    private var listeners = mutableListOf<Listener>()

    fun showBankChooser(
        fragmentManager: FragmentManager,
        link: String
    ) {
        BanksBottomSheetDialogFragment().apply {
            arguments = bundleOf(
                BanksBottomSheetDialogFragment.LINK to link
            )
        }.show(fragmentManager, BANKS_FRAGMENT_TAG)
    }

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    fun removeListeners() {
        listeners.clear()
    }

    internal fun notifyRedirectedToBankApp() {
        listeners.forEach {
            it.onRedirectedToBankApp()
        }
    }

    interface Listener {
        fun onRedirectedToBankApp()
    }

    private const val BANKS_FRAGMENT_TAG = "RaiffeisenBanksFragment"
}