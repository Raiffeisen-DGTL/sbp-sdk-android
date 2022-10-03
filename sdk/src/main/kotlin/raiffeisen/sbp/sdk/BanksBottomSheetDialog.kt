package raiffeisen.sbp.sdk

import android.content.Context
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

class BanksBottomSheetDialog(
    context: Context,
    theme: Int
) : BottomSheetDialog(context, theme) {

    private var restoredInstanceState: Bundle? = null

    override fun onSaveInstanceState() = super.onSaveInstanceState().apply {
        putInt(SavedInstanceArgument.BEHAVIOR_STATE, behavior.state)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        restoredInstanceState = savedInstanceState
    }

    override fun onStart() {
        super.onStart()
        behavior.peekHeight = context.resources.displayMetrics.heightPixels
        behavior.state = restoredInstanceState
            ?.getInt(SavedInstanceArgument.BEHAVIOR_STATE)
            ?: BottomSheetBehavior.STATE_EXPANDED
    }

    private object SavedInstanceArgument {
        const val BEHAVIOR_STATE = "BEHAVIOR_STATE"
    }
}