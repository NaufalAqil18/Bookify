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
import com.google.android.material.bottomsheet.BottomSheetDialog

class CheckoutActivity : AppCompatActivity() {

    private lateinit var addressText: TextView
    private lateinit var changeAddressButton: TextView
    private lateinit var addNewAddressButton: Button
    private lateinit var payOptionLayout: LinearLayout
    private lateinit var payButton: Button
    private lateinit var selectedPaymentMethodText: TextView

    private var deliveryAddress = "No.23, James Street, New\nTown, North Province"
    private var selectedPaymentMethod: PaymentMethod? = null

    private val paymentMethods = listOf(
        PaymentMethod("mandiri", "Mandiri", R.drawable.ic_mandiri),
        PaymentMethod("bca", "BCA", R.drawable.ic_bca),
        PaymentMethod("bni", "BNI", R.drawable.ic_bni),
        PaymentMethod("gopay", "Gopay", R.drawable.ic_gopay),
        PaymentMethod("dana", "Dana", R.drawable.ic_dana),
        PaymentMethod("ovo", "OVO", R.drawable.ic_ovo)
    )

    companion object {
        const val TAG = "CheckoutActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            setContentView(R.layout.activity_checkout)
            Log.d(TAG, "💳 CheckoutActivity created")

            if (CartManager.getCartItems().isEmpty()) {
                Log.w(TAG, "⚠️ Attempted to checkout with empty cart")
                Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show()
                navigateToCart()
                return
            }

            initViews()
            setupClickListeners()
            updateOrderSummary()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing CheckoutActivity", e)
            Toast.makeText(this, "Error loading checkout", Toast.LENGTH_SHORT).show()
            navigateToCart()
        }
    }

    private fun initViews() {
        try {
            addressText = findViewById(R.id.address_text)
            changeAddressButton = findViewById(R.id.btn_change_address)
            addNewAddressButton = findViewById(R.id.btn_add_new_address)
            payOptionLayout = findViewById(R.id.pay_option_layout)
            payButton = findViewById(R.id.btn_pay)
            selectedPaymentMethodText = findViewById(R.id.selected_payment_method)

            // Set initial address
            addressText.text = deliveryAddress

            Log.d(TAG, "✅ Views initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views", e)
            throw e
        }
    }

    private fun setupClickListeners() {
        try {
            changeAddressButton.setOnClickListener {
                Log.d(TAG, "📍 Change address clicked")
                showAddressOptions()
            }

            addNewAddressButton.setOnClickListener {
                Log.d(TAG, "➕ Add new address clicked")
                showAddNewAddressDialog()
            }

            payOptionLayout.setOnClickListener {
                Log.d(TAG, "💳 Pay option clicked")
                showPaymentOptions()
            }

            payButton.setOnClickListener {
                Log.d(TAG, "💰 Pay button clicked")
                if (selectedPaymentMethod == null) {
                    Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                processPayment()
            }

            Log.d(TAG, "✅ Click listeners setup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up click listeners", e)
            throw e
        }
    }

    private fun showPaymentOptions() {
        try {
            val bottomSheet = BottomSheetDialog(this)
            val view = layoutInflater.inflate(R.layout.bottom_sheet_payment_methods, null)
            bottomSheet.setContentView(view)

            val recyclerView = view.findViewById<RecyclerView>(R.id.payment_methods_recycler)
            recyclerView.layoutManager = LinearLayoutManager(this)
            
            val adapter = PaymentMethodAdapter(paymentMethods) { paymentMethod ->
                selectedPaymentMethod = paymentMethod
                selectedPaymentMethodText.text = paymentMethod.name
                bottomSheet.dismiss()
                Log.d(TAG, "Selected payment method: ${paymentMethod.name}")
            }
            
            recyclerView.adapter = adapter
            bottomSheet.show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing payment options", e)
            Toast.makeText(this, "Error loading payment methods", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateOrderSummary() {
        try {
            val total = CartManager.getTotal()
            payButton.text = "Pay $${String.format("%.2f", total)}"
            Log.d(TAG, "💰 Order summary updated - Total: $total")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating order summary", e)
            Toast.makeText(this, "Error updating total", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAddressOptions() {
        Toast.makeText(this, "Address selection feature coming soon!", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "💡 Address selection not implemented yet")
    }

    private fun showAddNewAddressDialog() {
        Toast.makeText(this, "Add new address feature coming soon!", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "💡 Add new address not implemented yet")
    }

    private fun processPayment() {
        try {
            val cartItems = CartManager.getCartItems()
            val total = CartManager.getTotal()
            val itemCount = CartManager.getCartItemCount()

            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show()
                navigateToCart()
                return
            }

            Log.d(TAG, "💳 Processing payment - Items: $itemCount, Total: $total")
            Log.d(TAG, "📍 Delivery Address: $deliveryAddress")
            Log.d(TAG, "💳 Payment Method: ${selectedPaymentMethod?.name}")

            // Disable UI during payment processing
            setLoadingState(true)

            // Simulate payment processing
            simulatePaymentProcess(total, itemCount)
        } catch (e: Exception) {
            Log.e(TAG, "Error processing payment", e)
            Toast.makeText(this, "Error processing payment", Toast.LENGTH_SHORT).show()
            setLoadingState(false)
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        payButton.isEnabled = !isLoading
        payButton.text = if (isLoading) "Processing..." else "Pay $${String.format("%.2f", CartManager.getTotal())}"
        payOptionLayout.isEnabled = !isLoading
        changeAddressButton.isEnabled = !isLoading
        addNewAddressButton.isEnabled = !isLoading
    }

    private fun simulatePaymentProcess(total: Double, itemCount: Int) {
        try {
            Log.d(TAG, "⏳ Simulating payment process...")

            // Simulate network delay
            payButton.postDelayed({
                try {
                    Log.d(TAG, "✅ Payment processed successfully")
                    
                    // Save purchase history
                    val cartItems = CartManager.getCartItems()
                    PurchaseHistoryManager.savePurchaseHistory(cartItems) { success ->
                        if (success) {
                            Log.d(TAG, "✅ Purchase history saved")
                        } else {
                            Log.e(TAG, "❌ Failed to save purchase history")
                        }
                        
                        // Clear the cart first
                        CartManager.clearCart()
                        
                        // Show confirmation screen
                        showConfirmationScreen()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error completing payment", e)
                    Toast.makeText(this, "Error completing payment", Toast.LENGTH_SHORT).show()
                    setLoadingState(false)
                }
            }, 2000) // 2 seconds delay to simulate processing
        } catch (e: Exception) {
            Log.e(TAG, "Error simulating payment", e)
            Toast.makeText(this, "Error processing payment", Toast.LENGTH_SHORT).show()
            setLoadingState(false)
        }
    }

    private fun showConfirmationScreen() {
        try {
            Log.d(TAG, "🎉 Showing confirmation screen")
            
            // Create and start confirmation activity
            val intent = Intent(this, CheckoutConfirmActivity::class.java)
            startActivity(intent)
            
            // Don't finish this activity yet
            // Let CheckoutConfirmActivity handle the navigation
        } catch (e: Exception) {
            Log.e(TAG, "Error showing confirmation screen", e)
            Toast.makeText(this, "Error showing confirmation", Toast.LENGTH_SHORT).show()
            navigateToHome()
        }
    }

    private fun navigateToHome() {
        try {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to home", e)
            finish()
        }
    }

    private fun navigateToCart() {
        try {
            val intent = Intent(this, CartActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to cart", e)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            if (CartManager.getCartItems().isEmpty()) {
                Log.w(TAG, "⚠️ Cart is empty, finishing checkout")
                Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show()
                navigateToCart()
            } else {
                updateOrderSummary()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onResume", e)
            navigateToCart()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "🔄 CheckoutActivity destroyed")
    }
}