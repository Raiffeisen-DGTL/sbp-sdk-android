package raiffeisen.sbp.sample

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import raiffeisen.sbp.sdk.BanksBottomSheetDialogFragment

class MainActivity : AppCompatActivity(R.layout.main_acitivty) {

    val link = "https://qr.nspk.ru/AD100004BAL7227F9BNP6KNE007J9B3K?type=02&bank=100000000007&sum=1&cur=RUB&crc=AB75"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val payButton: Button = findViewById(R.id.pay_button)
        payButton.setOnClickListener {
            BanksBottomSheetDialogFragment().apply {
                arguments = bundleOf(
                    BanksBottomSheetDialogFragment.LINK to link
                )
            }.show(supportFragmentManager, null)
        }
    }
}