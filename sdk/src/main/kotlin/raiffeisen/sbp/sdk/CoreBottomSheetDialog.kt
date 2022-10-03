package raiffeisen.sbp.sdk

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

abstract class CoreBottomSheetDialog(
    context: Context,
    theme: Int,
    private val initialBehaviorState: Int = BottomSheetBehavior.STATE_COLLAPSED,
    private val fullscreen: Boolean = false
) : BottomSheetDialog(context, theme) {

    protected var restoredInstanceState: Bundle? = null
        private set

    protected val bottomSheetLayout
        get() = checkNotNull(
            findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
        )

    override fun onSaveInstanceState() = super.onSaveInstanceState().apply {
        putInt(SavedInstanceArgument.BEHAVIOR_STATE, behavior.state)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        restoredInstanceState = savedInstanceState
    }

    override fun setContentView(view: View) {
        super.setContentView(view)
        bottomSheetLayout.updateLayoutParams {
            height = if (fullscreen) MATCH_PARENT else WRAP_CONTENT
        }
    }

    override fun onStart() {
        super.onStart()
        behavior.state = restoredInstanceState
            ?.getInt(SavedInstanceArgument.BEHAVIOR_STATE)
            ?: initialBehaviorState
    }

    private object SavedInstanceArgument {
        const val BEHAVIOR_STATE = "BEHAVIOR_STATE"
    }
}