package id.usk.ac.bookify

import android.util.Log

object CartManager {
    private const val TAG = "CartManager"
    private val cartItems = mutableListOf<CartItem>()
    private val listeners = mutableListOf<CartUpdateListener>()

    interface CartUpdateListener {
        fun onCartUpdated()
    }

    fun addToCart(book: Book) {
        val existingItem = cartItems.find { it.book.bookId == book.bookId }

        if (existingItem != null) {
            existingItem.quantity++
            Log.d(TAG, "📈 Updated quantity for ${book.title}: ${existingItem.quantity}")
        } else {
            cartItems.add(CartItem(book, 1))
            Log.d(TAG, "➕ Added new item to cart: ${book.title}")
        }

        notifyListeners()
    }

    fun removeFromCart(cartItem: CartItem) {
        cartItems.remove(cartItem)
        Log.d(TAG, "🗑️ Removed item from cart: ${cartItem.book.title}")
        notifyListeners()
    }

    fun updateQuantity(cartItem: CartItem, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeFromCart(cartItem)
        } else {
            cartItem.quantity = newQuantity
            Log.d(TAG, "🔄 Updated quantity for ${cartItem.book.title}: $newQuantity")
            notifyListeners()
        }
    }

    fun getCartItems(): MutableList<CartItem> {
        return cartItems.toMutableList() // Return a copy to prevent external modification
    }

    fun getCartItemCount(): Int {
        return cartItems.sumOf { it.quantity }
    }

    fun getSubtotal(): Double {
        return cartItems.sumOf { it.getTotalPrice() }
    }

    fun getShipping(): Double {
        return if (cartItems.isEmpty()) 0.0 else 10.0 // Free shipping if cart is empty
    }

    fun getTotal(): Double {
        return getSubtotal() + getShipping()
    }

    fun clearCart() {
        cartItems.clear()
        Log.d(TAG, "🧹 Cart cleared")
        notifyListeners()
    }

    fun isInCart(bookId: String): Boolean {
        return cartItems.any { it.book.bookId == bookId }
    }

    fun addCartUpdateListener(listener: CartUpdateListener) {
        listeners.add(listener)
    }

    fun removeCartUpdateListener(listener: CartUpdateListener) {
        listeners.remove(listener)
    }

    private fun notifyListeners() {
        listeners.forEach { it.onCartUpdated() }
        Log.d(TAG, "📢 Notified ${listeners.size} listeners of cart update")
    }
}