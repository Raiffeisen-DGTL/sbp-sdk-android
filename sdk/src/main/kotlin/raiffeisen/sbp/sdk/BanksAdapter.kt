package raiffeisen.sbp.sdk

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade


class BanksAdapter(
    private val onBankClicked: (Item.Bank) -> Unit
) : ListAdapter<BanksAdapter.Item, ViewHolder>(ItemDiffUtilCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        Item.Header::class.hashCode() -> HeaderViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.header_item,
                parent,
                false
            )
        )
        Item.Bank::class.hashCode() -> BankViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.bank_item,
                parent,
                false
            )
        )
        else -> error("Unexpected viewType: $viewType")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> holder.bind(getItem(position) as Item.Header)
            is BankViewHolder -> holder.bind(getItem(position) as Item.Bank)
        }
    }

    private inner class HeaderViewHolder(itemView: View) : ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.title_textView)

        fun bind(item: Item.Header) {
            title.text = item.value
        }
    }

    private inner class BankViewHolder(itemView: View) : ViewHolder(itemView) {
        private val icon: ImageView = itemView.findViewById(R.id.icon_imageView)
        private val name: TextView = itemView.findViewById(R.id.name_textView)

        fun bind(item: Item.Bank) {
            itemView.setOnClickListener {
                onBankClicked(item)
            }

            name.text = item.info.name
            Glide.with(icon)
                .load(item.info.logoUrl)
                .transition(withCrossFade())
                .transform(
                    RoundedCorners(
                        itemView.resources.getDimensionPixelSize(R.dimen.app_icon_corner_radius)
                    )
                )
                .into(icon)
        }
    }

    override fun getItemViewType(position: Int) = getItem(position)::class.hashCode()

    sealed interface Item {
        data class Header(val value: String) : Item
        data class Bank(val info: BankAppInfo) : Item
    }
}

private object ItemDiffUtilCallback : DiffUtil.ItemCallback<BanksAdapter.Item>() {
    override fun areItemsTheSame(
        oldItem: BanksAdapter.Item,
        newItem: BanksAdapter.Item
    ) = oldItem == newItem

    override fun areContentsTheSame(
        oldItem: BanksAdapter.Item,
        newItem: BanksAdapter.Item
    ) = oldItem == newItem
}