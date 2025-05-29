package id.usk.ac.bookify

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView

class CartActivity : AppCompatActivity(), CartManager.CartUpdateListener {

    private lateinit var cartRecyclerView: RecyclerView
    private lateinit var emptyCartLayout: LinearLayout
    private lateinit var cartContentLayout: LinearLayout
    private lateinit var orderSummaryLayout: LinearLayout

    private lateinit var subtotalValue: TextView
    private lateinit var shippingValue: TextView
    private lateinit var totalValue: TextView

    private lateinit var btnContinueShopping: Button
    private lateinit var btnCheckout: Button

    private lateinit var cartAdapter: CartAdapter

    companion object {
        const val TAG = "CartActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        Log.d(TAG, "🛒 CartActivity created")

        initViews()
        setupRecyclerView()
        setupClickListeners()
        setupBottomNavigation()

        // Register for cart updates
        CartManager.addCartUpdateListener(this)

        updateUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        CartManager.removeCartUpdateListener(this)
        Log.d(TAG, "🔄 CartActivity destroyed")
    }

    private fun initViews() {
        cartRecyclerView = findViewById(R.id.cart_recycler_view)
        emptyCartLayout = findViewById(R.id.empty_cart_layout)
        cartContentLayout = findViewById(R.id.cart_content_layout)
        orderSummaryLayout = findViewById(R.id.order_summary_layout)

        subtotalValue = findViewById(R.id.subtotal_value)
        shippingValue = findViewById(R.id.shipping_value)
        totalValue = findViewById(R.id.total_value)

        btnContinueShopping = findViewById(R.id.btn_continue_shopping)
        btnCheckout = findViewById(R.id.btn_checkout)

        Log.d(TAG, "✅ Views initialized")
    }

    private fun setupBottomNavigation() {
        try {
            findViewById<LinearLayout>(R.id.nav_homepage).setOnClickListener {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }

            findViewById<LinearLayout>(R.id.nav_categories).setOnClickListener {
                startActivity(Intent(this, CategoriesActivity::class.java))
                finish()
            }

            // Cart is current activity, no need to handle

            findViewById<LinearLayout>(R.id.nav_profile).setOnClickListener {
                startActivity(Intent(this, ProfileActivity::class.java))
                finish()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up navigation", e)
            Toast.makeText(this, "Error setting up navigation", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            cartItems = mutableListOf(),
            onItemUpdate = { cartItem ->
                Log.d(TAG, "🔄 Item updated: ${cartItem.book.title}")
                updateOrderSummary()
            },
            onItemRemove = { cartItem ->
                Log.d(TAG, "🗑️ Removing item: ${cartItem.book.title}")
                CartManager.removeFromCart(cartItem)
                cartAdapter.removeItem(cartItem)

                if (CartManager.getCartItems().isEmpty()) {
                    updateUI()
                }
            }
        )

        cartRecyclerView.apply {
            adapter = cartAdapter
            layoutManager = LinearLayoutManager(this@CartActivity)
        }

        Log.d(TAG, "✅ RecyclerView setup completed")
    }

    private fun setupClickListeners() {
        btnContinueShopping.setOnClickListener {
            Log.d(TAG, "🔙 Continue shopping clicked")
            finish() // Go back to previous activity (MainActivity)
        }

        btnCheckout.setOnClickListener {
            Log.d(TAG, "💳 Checkout clicked")
            proceedToCheckout()
        }

        Log.d(TAG, "✅ Click listeners setup completed")
    }

    private fun updateUI() {
        val cartItems = CartManager.getCartItems()

        Log.d(TAG, "🔄 Updating UI - Cart items: ${cartItems.size}")

        if (cartItems.isEmpty()) {
            showEmptyCart()
        } else {
            showCartContent(cartItems)
        }
    }

    private fun showEmptyCart() {
        Log.d(TAG, "📭 Showing empty cart state")

        emptyCartLayout.visibility = View.VISIBLE
        cartContentLayout.visibility = View.GONE
    }

    private fun showCartContent(cartItems: MutableList<CartItem>) {
        Log.d(TAG, "🛒 Showing cart content with ${cartItems.size} items")

        emptyCartLayout.visibility = View.GONE
        cartContentLayout.visibility = View.VISIBLE

        cartAdapter.updateCartItems(cartItems)
        updateOrderSummary()
    }

    private fun updateOrderSummary() {
        val subtotal = CartManager.getSubtotal()
        val shipping = CartManager.getShipping()
        val total = CartManager.getTotal()

        subtotalValue.text = "$${String.format("%.2f", subtotal)}"
        shippingValue.text = "$${String.format("%.2f", shipping)}"
        totalValue.text = "$${String.format("%.2f", total)}"

        Log.d(TAG, "💰 Order summary updated - Subtotal: $subtotal, Total: $total")
    }

    private fun proceedToCheckout() {
        val cartItems = CartManager.getCartItems()

        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show()
            return
        }

        val total = CartManager.getTotal()
        val itemCount = CartManager.getCartItemCount()

        Log.d(TAG, "💳 Proceeding to checkout - Items: $itemCount, Total: $total")

        // Navigate to CheckoutActivity
        val intent = Intent(this, CheckoutActivity::class.java)
        startActivity(intent)
    }

    override fun onCartUpdated() {
        runOnUiThread {
            Log.d(TAG, "📢 Cart update notification received")
            updateUI()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "🔄 CartActivity resumed - refreshing UI")
        updateUI()
    }
}