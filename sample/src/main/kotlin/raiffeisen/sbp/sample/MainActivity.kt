package raiffeisen.sbp.sample

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import raiffeisen.sbp.sdk.RaiffeisenSbpSdk

class MainActivity : AppCompatActivity(R.layout.main_acitivty) {

    private val sdkListener = object : RaiffeisenSbpSdk.Listener {
        override fun onRedirectedToBankApp() {
            Toast.makeText(
                this@MainActivity,
                "onRedirectedToBankApp",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val linkEditText: EditText = findViewById(R.id.link_editText)
        val payButton: Button = findViewById(R.id.pay_button)

        payButton.setOnClickListener {
            RaiffeisenSbpSdk.showBankChooser(
                supportFragmentManager,
                linkEditText.text.toString()
            )
        }

        RaiffeisenSbpSdk.addListener(sdkListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        RaiffeisenSbpSdk.removeListener(sdkListener)
    }
}