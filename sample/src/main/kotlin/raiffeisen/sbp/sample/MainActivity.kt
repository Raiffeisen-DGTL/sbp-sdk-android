package raiffeisen.sbp.sample

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import raiffeisen.sbp.sdk.BanksBottomSheetDialogFragment

class MainActivity : AppCompatActivity(R.layout.main_acitivty) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val payButton: Button = findViewById(R.id.pay_button)
        payButton.setOnClickListener {
            BanksBottomSheetDialogFragment().show(supportFragmentManager, null)
        }
    }
}