package raiffeisen.sbp.sdk

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
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
                    BanksViewModel(BanksRepository())
                }
            }
        )[BanksViewModel::class.java]
    }

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
        val banksSpanCount = calculateSpanCount(
            spanDp = 100f,
            maxSpanCount = 5
        )

        view.findViewById<View>(R.id.close_button).setOnClickListener {
            dismiss()
        }

        val clearSearchButton: Button = view.findViewById(R.id.clearSearch_button)
        clearSearchButton.setOnClickListener {
            viewModel.setSearchText("")
        }

        val searchLayout = view.findViewById<LinearLayout>(R.id.search_layout)
        val searchEditText = view.findViewById<EditText>(R.id.search_editText)
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

        val banksRecyclerView: RecyclerView = view.findViewById(R.id.banks_recyclerView)
        val banksLayoutManager = GridLayoutManager(context, banksSpanCount)
        val banksAdapter = BanksAdapter(
            onBankClicked = {

            }
        )

        banksRecyclerView.layoutManager = banksLayoutManager
        banksRecyclerView.adapter = banksAdapter

        viewModel.state.onEach { state ->
            if (searchEditText.text?.toString().orEmpty() != state.searchText) {
                searchEditText.setText(state.searchText)
            }

            clearSearchButton.isVisible = state.searchText.isNotEmpty()

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

            banksLayoutManager.spanSizeLookup = object : SpanSizeLookup() {
                override fun getSpanSize(position: Int) = when (items[position]) {
                    is BanksAdapter.Item.Header -> banksSpanCount
                    is BanksAdapter.Item.Bank -> 1
                }
            }

            banksAdapter.submitList(items)
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun calculateSpanCount(spanDp: Float, maxSpanCount: Int): Int {
        val metrics = resources.displayMetrics
        val spanPx = spanDp * metrics.density
        val spanCount = (metrics.widthPixels / spanPx).roundToInt()
        return if (spanCount > maxSpanCount) maxSpanCount else spanCount
    }
}