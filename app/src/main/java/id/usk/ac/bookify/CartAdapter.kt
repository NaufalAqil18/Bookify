package id.usk.ac.bookify

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CartAdapter(
    private var cartItems: MutableList<CartItem>,
    private val onItemUpdate: (CartItem) -> Unit,
    private val onItemRemove: (CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    companion object {
        private const val TAG = "CartAdapter"
    }

    class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val bookCover: ImageView = view.findViewById(R.id.book_cover)
        val bookCategory: TextView = view.findViewById(R.id.bookCategory)
        val bookTitle: TextView = view.findViewById(R.id.book_title)
        val bookAuthor: TextView = view.findViewById(R.id.bookAuthor)
        val bookPrice: TextView = view.findViewById(R.id.bookPrice)
        val quantityText: TextView = view.findViewById(R.id.quantity_text)
        val btnMinus: ImageView = view.findViewById(R.id.btn_minus)
        val btnPlus: ImageView = view.findViewById(R.id.btn_plus)
        val btnDelete: ImageView = view.findViewById(R.id.btn_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        try {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_cart, parent, false)
            return CartViewHolder(view)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating view holder", e)
            throw e
        }
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        try {
            val cartItem = cartItems.getOrNull(position) ?: run {
                Log.e(TAG, "Invalid position: $position")
                return
            }
            
            val book = cartItem.book

            Log.d(TAG, "🛒 Binding cart item: ${book.title} (Qty: ${cartItem.quantity})")

            // Set book information
            holder.bookCategory.text = book.category.orEmpty()
            holder.bookTitle.text = book.title
            holder.bookAuthor.text = book.author
            holder.quantityText.text = cartItem.quantity.toString()
            holder.bookPrice.text = "$${String.format("%.2f", cartItem.getTotalPrice())}"

            // Load book cover image
            try {
                Glide.with(holder.itemView.context)
                    .load(book.imageUrl)
                    .placeholder(R.drawable.picture3)
                    .error(R.drawable.picture3)
                    .centerCrop()
                    .into(holder.bookCover)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading book cover image", e)
                holder.bookCover.setImageResource(R.drawable.picture3)
            }

            // Set up quantity controls
            holder.btnMinus.setOnClickListener {
                if (cartItem.quantity > 1) {
                    cartItem.quantity--
                    holder.quantityText.text = cartItem.quantity.toString()
                    holder.bookPrice.text = "$${String.format("%.2f", cartItem.getTotalPrice())}"
                    onItemUpdate(cartItem)
                    Log.d(TAG, "➖ Decreased quantity for ${book.title}: ${cartItem.quantity}")
                }
            }

            holder.btnPlus.setOnClickListener {
                cartItem.quantity++
                holder.quantityText.text = cartItem.quantity.toString()
                holder.bookPrice.text = "$${String.format("%.2f", cartItem.getTotalPrice())}"
                onItemUpdate(cartItem)
                Log.d(TAG, "➕ Increased quantity for ${book.title}: ${cartItem.quantity}")
            }

            // Set up delete button
            holder.btnDelete.setOnClickListener {
                Log.d(TAG, "🗑️ Removing item from cart: ${book.title}")
                onItemRemove(cartItem)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error binding view holder at position $position", e)
        }
    }

    override fun getItemCount() = cartItems.size

    fun updateCartItems(newCartItems: MutableList<CartItem>) {
        try {
            cartItems = newCartItems
            notifyDataSetChanged()
            Log.d(TAG, "📊 Cart adapter updated with ${newCartItems.size} items")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating cart items", e)
        }
    }

    fun removeItem(cartItem: CartItem) {
        try {
            val position = cartItems.indexOf(cartItem)
            if (position != -1) {
                cartItems.removeAt(position)
                notifyItemRemoved(position)
                Log.d(TAG, "✅ Item removed from cart: ${cartItem.book.title}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error removing item from cart", e)
        }
    }
}