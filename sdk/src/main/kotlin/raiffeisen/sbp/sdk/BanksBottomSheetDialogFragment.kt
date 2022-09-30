package raiffeisen.sbp.sdk

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.math.roundToInt


class BanksBottomSheetDialogFragment : BottomSheetDialogFragment() {

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

    private val linkFromArgs
        get() = requireArguments().getString(LINK)
            ?: error("BanksBottomSheetDialogFragment require LINK argument")

    override fun getTheme() = R.style.BanksBottomSheetDialogTheme

    override fun onCreateDialog(
        savedInstanceState: Bundle?
    ) = BanksBottomSheetDialog(requireContext(), theme)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.banks_bottom_sheet, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val closeButton = view.findViewById<View>(R.id.close_button)
        val clearSearchButton = view.findViewById<Button>(R.id.clearSearch_button)
        val openDefaultBankButton = view.findViewById<Button>(R.id.openDefaultBank_button)
        val openDefaultBankTextView = view.findViewById<TextView>(R.id.openDefaultBank_textView)
        val searchLayout = view.findViewById<LinearLayout>(R.id.search_layout)
        val searchEditText = view.findViewById<EditText>(R.id.search_editText)
        val banksRecyclerView = view.findViewById<RecyclerView>(R.id.banks_recyclerView)

        val banksSpanCount = calculateSpanCount(
            spanDp = 100f,
            maxSpanCount = 5
        )

        openDefaultBankButton.setOnClickListener {
            // TODO
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
                searchLayout.setBackgroundResource(R.drawable.search_border_focused)
            } else {
                searchLayout.setBackgroundResource(R.drawable.search_border)
            }
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
                        add(BanksAdapter.Item.Header(getString(R.string.recent_banks_title)))
                    }
                    addAll(state.recentBanks.map(BanksAdapter.Item::Bank))
                }

                if (state.allBanks.isNotEmpty()) {
                    if (state.recentBanks.isNotEmpty()) {
                        add(BanksAdapter.Item.Header(getString(R.string.all_banks_title)))
                    }
                    addAll(state.allBanks.map(BanksAdapter.Item::Bank))
                }
            }

            banksAdapter.submitList(items)
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun redirectToBank(bankAppInfo: BankAppInfo) {
        try {
            val formattedLink = linkFromArgs.replaceBefore(':', bankAppInfo.schema)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(formattedLink)
            startActivity(intent)
            viewModel.saveBankRedirected(bankAppInfo)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, R.string.bank_open_error, Toast.LENGTH_SHORT).show()
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

    companion object {
        const val LINK = "LINK"
    }
}