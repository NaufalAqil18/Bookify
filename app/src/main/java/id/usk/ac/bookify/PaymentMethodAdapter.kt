package id.usk.ac.bookify

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PaymentMethodAdapter(
    private val paymentMethods: List<PaymentMethod>,
    private val onItemClick: (PaymentMethod) -> Unit
) : RecyclerView.Adapter<PaymentMethodAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.payment_icon)
        val name: TextView = view.findViewById(R.id.payment_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_payment_method, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val paymentMethod = paymentMethods[position]
        holder.icon.setImageResource(paymentMethod.iconRes)
        holder.name.text = paymentMethod.name
        holder.itemView.setOnClickListener { onItemClick(paymentMethod) }
    }

    override fun getItemCount() = paymentMethods.size
}

data class PaymentMethod(
    val id: String,
    val name: String,
    val iconRes: Int
) 