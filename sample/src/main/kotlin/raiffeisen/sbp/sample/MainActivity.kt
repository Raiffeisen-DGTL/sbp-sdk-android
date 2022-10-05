package raiffeisen.sbp.sample

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import raiffeisen.sbp.sdk.SbpRedirectFragment

class MainActivity : AppCompatActivity(R.layout.main_acitivty) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val linkEditText: EditText = findViewById(R.id.link_editText)
        val payButton: Button = findViewById(R.id.pay_button)

        payButton.setOnClickListener {
            openSbpRedirectDialog(linkEditText.text.toString())
        }

        supportFragmentManager.setFragmentResultListener(
            SBP_RESULT_KEY,
            this
        ) { _, bundle ->
            when (val result = SbpRedirectFragment.readResult(bundle)) {
                is SbpRedirectFragment.Result.RedirectedToBank -> {
                    toast("redirected to bank ${result.packageName}")
                }
                is SbpRedirectFragment.Result.RedirectedToDownloadBank -> {
                    toast("redirected to download bank ${result.packageName}")
                }
                SbpRedirectFragment.Result.RedirectedToDefaultBank -> {
                    toast("redirected to default bank")
                }
                SbpRedirectFragment.Result.DialogDismissed -> {
                    toast("dialog dismissed")
                }
                is SbpRedirectFragment.Result.RedirectToBankFailed -> {
                    toast("redirect to bank failed ${result.packageName}")
                }
            }
            closeSbpRedirectDialog()
        }
    }

    private fun openSbpRedirectDialog(link: String) {
        SbpRedirectFragment.newInstance(
            link = link,
            resultKey = SBP_RESULT_KEY
        ).show(supportFragmentManager, SBP_REDIRECT_FRAGMENT_TAG)
    }

    private fun closeSbpRedirectDialog() {
        val fragment = supportFragmentManager.findFragmentByTag(SBP_REDIRECT_FRAGMENT_TAG)
        if (fragment is SbpRedirectFragment) fragment.dismiss()
    }

    private fun toast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val SBP_REDIRECT_FRAGMENT_TAG = "sbpRedirect"
        const val SBP_RESULT_KEY = "sbpResultKey"
    }
}