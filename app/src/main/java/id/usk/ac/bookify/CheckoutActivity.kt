package id.usk.ac.bookify

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CheckoutActivity : AppCompatActivity() {

    private lateinit var addressText: TextView
    private lateinit var changeAddressButton: TextView
    private lateinit var addNewAddressButton: Button
    private lateinit var payOptionLayout: LinearLayout
    private lateinit var payButton: Button

    private var deliveryAddress = "No.23, James Street, New\nTown, North Province"
    private var selectedPaymentMethod = "Credit Card"

    companion object {
        const val TAG = "CheckoutActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        Log.d(TAG, "💳 CheckoutActivity created")

        initViews()
        setupClickListeners()
        updateOrderSummary()
    }

    private fun initViews() {
        addressText = findViewById(R.id.address_text)
        changeAddressButton = findViewById(R.id.btn_change_address)
        addNewAddressButton = findViewById(R.id.btn_add_new_address)
        payOptionLayout = findViewById(R.id.pay_option_layout)
        payButton = findViewById(R.id.btn_pay)

        // Set initial address
        addressText.text = deliveryAddress

        Log.d(TAG, "✅ Views initialized")
    }

    private fun setupClickListeners() {
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
            processPayment()
        }

        Log.d(TAG, "✅ Click listeners setup completed")
    }

    private fun updateOrderSummary() {
        val total = CartManager.getTotal()
        payButton.text = "Pay $${String.format("%.2f", total)}"

        Log.d(TAG, "💰 Order summary updated - Total: $total")
    }

    private fun showAddressOptions() {
        // For now, show a toast. In a real app, you would show a dialog or new activity
        Toast.makeText(this, "Address selection feature coming soon!", Toast.LENGTH_SHORT).show()

        Log.d(TAG, "💡 Address selection not implemented yet")
    }

    private fun showAddNewAddressDialog() {
        // For now, show a toast. In a real app, you would show a dialog or new activity
        Toast.makeText(this, "Add new address feature coming soon!", Toast.LENGTH_SHORT).show()

        Log.d(TAG, "💡 Add new address not implemented yet")
    }

    private fun showPaymentOptions() {
        // For now, show a toast. In a real app, you would show a dialog with payment options
        Toast.makeText(this, "Payment method selection coming soon!", Toast.LENGTH_SHORT).show()

        Log.d(TAG, "💡 Payment method selection not implemented yet")
    }

    private fun processPayment() {
        val cartItems = CartManager.getCartItems()
        val total = CartManager.getTotal()
        val itemCount = CartManager.getCartItemCount()

        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Log.d(TAG, "💳 Processing payment - Items: $itemCount, Total: $total")
        Log.d(TAG, "📍 Delivery Address: $deliveryAddress")
        Log.d(TAG, "💳 Payment Method: $selectedPaymentMethod")

        // Simulate payment processing
        simulatePaymentProcess(total, itemCount)
    }

    private fun simulatePaymentProcess(total: Double, itemCount: Int) {
        Log.d(TAG, "⏳ Simulating payment process...")

        // Show loading state
        payButton.text = "Processing..."
        payButton.isEnabled = false

        // Simulate network delay
        payButton.postDelayed({
            // Simulate successful payment
            Log.d(TAG, "✅ Payment processed successfully")
            Log.d(TAG, "📦 Order created - Items: $itemCount, Total: $total")

            // Clear the cart after successful payment
            CartManager.clearCart()

            // Navigate to success screen
            navigateToSuccessScreen()

        }, 2000) // 2 seconds delay to simulate processing
    }

    private fun navigateToSuccessScreen() {
        Log.d(TAG, "🎉 Navigating to success screen")

        val intent = Intent(this, CheckoutConfirmActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        // Update order summary when returning to this activity
        updateOrderSummary()

        // Check if cart is still not empty
        if (CartManager.getCartItems().isEmpty()) {
            Log.w(TAG, "⚠️ Cart is empty, finishing checkout")
            Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "🔄 CheckoutActivity destroyed")
    }
}