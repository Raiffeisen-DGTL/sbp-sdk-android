package raiffeisen.sbp.sdk.android

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


class SbpRedirectFragment : BottomSheetDialogFragment() {

    private val viewModel by lazy {
        ViewModelProvider(
            owner = this,
            factory = viewModelFactory {
                addInitializer(BanksViewModel::class) {
                    createViewModel()
                }
            }
        )[BanksViewModel::class.java]
    }

    private val playMarketAppChecker = PlayMarketAppChecker()

    private val linkFromArgs
        get() = arguments?.getString(LINK)
            ?: error("SbpRedirectFragment require $LINK argument")

    private val resultKeyFromArgs
        get() = arguments?.getString(RESULT_KEY) ?: DEFAULT_RESULT_KEY

    override fun getTheme() = R.style.Sbp_BanksBottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.sbp_redirect_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val closeButton = view.findViewById<View>(R.id.close_button)
        val clearSearchButton = view.findViewById<Button>(R.id.clearSearch_button)
        val openDefaultBankButton = view.findViewById<Button>(R.id.openDefaultBank_button)
        val openDefaultBankTextView = view.findViewById<TextView>(R.id.openDefaultBank_textView)
        val searchLayout = view.findViewById<LinearLayout>(R.id.search_layout)
        val searchEditText = view.findViewById<EditText>(R.id.search_editText)
        val banksRecyclerView = view.findViewById<RecyclerView>(R.id.banks_recyclerView)

        setupBottomSheetSize()

        val banksSpanCount = calculateSpanCount(
            spanDp = BANKS_SPAN_WIDTH_DP,
            maxSpanCount = BANKS_MAX_SPAN_COUNT
        )

        openDefaultBankButton.setOnClickListener {
            redirectToDefaultBank()
        }

        closeButton.setOnClickListener {
            dismiss()
        }

        clearSearchButton.setOnClickListener {
            viewModel.setSearchText("")
        }

        searchEditText.doOnTextChanged { text, _, _, _ ->
            viewModel.setSearchText(text?.toString().orEmpty())
        }

        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                searchLayout.setBackgroundResource(R.drawable.sbp_search_border_focused)
            } else {
                searchLayout.setBackgroundResource(R.drawable.sbp_search_border)
            }
        }

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                true
            } else false
        }

        val banksLayoutManager = GridLayoutManager(context, banksSpanCount)
        val banksAdapter = BanksAdapter(
            onBankClicked = { redirectToBank(it.info) }
        )

        banksRecyclerView.layoutManager = banksLayoutManager
        banksRecyclerView.adapter = banksAdapter

        banksLayoutManager.spanSizeLookup = object : SpanSizeLookup() {
            override fun getSpanSize(position: Int) = when (banksAdapter.currentList[position]) {
                is BanksAdapter.Item.Header -> banksSpanCount
                is BanksAdapter.Item.Bank -> 1
            }
        }

        viewModel.state.onEach { state ->
            if (searchEditText.text?.toString().orEmpty() != state.searchText) {
                searchEditText.setText(state.searchText)
            }

            clearSearchButton.isVisible = state.searchText.isNotEmpty()
            openDefaultBankTextView.isVisible = state.searchText.isNotEmpty()

            val items = mutableListOf<BanksAdapter.Item>().apply {
                if (state.recentBanks.isNotEmpty()) {
                    if (state.allBanks.isNotEmpty()) {
                        add(BanksAdapter.Item.Header(getString(R.string.sbp_recent_banks_title)))
                    }
                    addAll(state.recentBanks.map(BanksAdapter.Item::Bank))
                }

                if (state.allBanks.isNotEmpty()) {
                    if (state.recentBanks.isNotEmpty()) {
                        add(BanksAdapter.Item.Header(getString(R.string.sbp_all_banks_title)))
                    }
                    addAll(state.allBanks.map(BanksAdapter.Item::Bank))
                }
            }

            banksAdapter.submitList(items)
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        hideKeyboard()
        parentFragmentManager.setFragmentResult(
            resultKeyFromArgs,
            bundleOf(RESULT_DIALOG_DISMISSED to null)
        )
    }

    private fun setupBottomSheetSize() {
        (dialog as BottomSheetDialog).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)?.apply {
                updateLayoutParams {
                    height = (resources.displayMetrics.heightPixels / 2).let { newHeight ->
                        val minHeight = resources.getDimension(R.dimen.sbp_bottom_sheet_min_height)
                        if (newHeight < minHeight) minHeight.toInt()
                        else newHeight
                    }
                }
            }
        }
    }

    private fun redirectToBank(bankAppInfo: BankAppInfo) {
        if (bankAppInfo.packageName == null) {
            parentFragmentManager.setFragmentResult(
                resultKeyFromArgs,
                bundleOf(RESULT_REDIRECT_TO_BANK_FAILED to null)
            )

            return
        }

        try {
            val formattedLink = linkFromArgs.replaceBefore(':', bankAppInfo.schema)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(formattedLink)
            startActivity(intent)
            viewModel.saveBankRedirected(bankAppInfo)
            parentFragmentManager.setFragmentResult(
                resultKeyFromArgs,
                bundleOf(RESULT_REDIRECTED_TO_BANK to bankAppInfo.packageName)
            )
        } catch (e: ActivityNotFoundException) {
            lifecycleScope.launch {
                if (playMarketAppChecker.checkPackageExists(bankAppInfo.packageName)) {
                    goToAppInPlayMarket(bankAppInfo.packageName)
                    parentFragmentManager.setFragmentResult(
                        resultKeyFromArgs,
                        bundleOf(RESULT_REDIRECTED_TO_DOWNLOAD_BANK to bankAppInfo.packageName)
                    )
                } else {
                    parentFragmentManager.setFragmentResult(
                        resultKeyFromArgs,
                        bundleOf(RESULT_REDIRECT_TO_BANK_FAILED to bankAppInfo.packageName)
                    )
                }
            }
        } catch (e: Exception) {
            parentFragmentManager.setFragmentResult(
                resultKeyFromArgs,
                bundleOf(RESULT_REDIRECT_TO_BANK_FAILED to bankAppInfo.packageName)
            )
        }
    }

    private fun redirectToDefaultBank() {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(linkFromArgs)
            startActivity(intent)
            parentFragmentManager.setFragmentResult(
                resultKeyFromArgs,
                bundleOf(RESULT_REDIRECTED_TO_DEFAULT_BANK to null)
            )
        } catch (e: Exception) {
            parentFragmentManager.setFragmentResult(
                resultKeyFromArgs,
                bundleOf(RESULT_REDIRECT_TO_BANK_FAILED to null)
            )
        }
    }

    private fun goToAppInPlayMarket(packageName: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=$packageName")
                )
            )
        }
    }

    private fun createViewModel() = BanksViewModel(
        banksRepository = BanksRepository(
            context = requireContext().applicationContext
        ),
        packageManager = requireContext().packageManager
    )

    private fun calculateSpanCount(spanDp: Float, maxSpanCount: Int): Int {
        val metrics = resources.displayMetrics
        val spanPx = spanDp * metrics.density
        val spanCount = (metrics.widthPixels / spanPx).roundToInt()
        return if (spanCount > maxSpanCount) maxSpanCount else spanCount
    }

    private fun hideKeyboard() {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    companion object {
        private const val BANKS_SPAN_WIDTH_DP = 100f
        private const val BANKS_MAX_SPAN_COUNT = 5
        private const val LINK = "link"
        private const val RESULT_KEY = "resultKey"
        const val DEFAULT_RESULT_KEY = "sbpSdkResult"

        const val RESULT_REDIRECTED_TO_BANK = "REDIRECTED_TO_BANK"
        const val RESULT_REDIRECTED_TO_DEFAULT_BANK = "REDIRECTED_TO_DEFAULT_BANK"
        const val RESULT_REDIRECTED_TO_DOWNLOAD_BANK = "REDIRECTED_TO_DOWNLOAD_BANK"
        const val RESULT_REDIRECT_TO_BANK_FAILED = "RESULT_REDIRECTED_TO_BANK_FAILED"
        const val RESULT_DIALOG_DISMISSED = "RESULT_DIALOG_DISMISSED"

        fun newInstance(
            link: String,
            resultKey: String = DEFAULT_RESULT_KEY
        ) = SbpRedirectFragment().apply {
            arguments = bundleOf(
                LINK to link,
                RESULT_KEY to resultKey,
            )
        }

        fun readResult(bundle: Bundle): Result = when {
            bundle.containsKey(RESULT_REDIRECTED_TO_BANK) -> {
                Result.RedirectedToBank(
                    bundle.getString(RESULT_REDIRECTED_TO_BANK)!!
                )
            }
            bundle.containsKey(RESULT_REDIRECTED_TO_DOWNLOAD_BANK) -> {
                Result.RedirectedToDownloadBank(
                    bundle.getString(
                        RESULT_REDIRECTED_TO_DOWNLOAD_BANK
                    )!!
                )
            }
            bundle.containsKey(RESULT_REDIRECTED_TO_DEFAULT_BANK) -> {
                Result.RedirectedToDefaultBank
            }
            bundle.containsKey(RESULT_REDIRECT_TO_BANK_FAILED) -> {
                Result.RedirectToBankFailed(bundle.getString(RESULT_REDIRECT_TO_BANK_FAILED))
            }
            bundle.containsKey(RESULT_DIALOG_DISMISSED) -> {
                Result.DialogDismissed
            }
            else -> error("Cant read result")
        }
    }

    sealed interface Result {
        class RedirectedToBank(val packageName: String?) : Result
        class RedirectedToDownloadBank(val packageName: String?) : Result
        object RedirectedToDefaultBank : Result
        class RedirectToBankFailed(val packageName: String?) : Result
        object DialogDismissed : Result
    }
}