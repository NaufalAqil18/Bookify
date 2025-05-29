package id.usk.ac.bookify

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class CheckoutConfirmActivity : AppCompatActivity() {

    private lateinit var closeButton: ImageView

    companion object {
        const val TAG = "CheckoutConfirmActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout_confirm)

        Log.d(TAG, "🎉 CheckoutConfirmActivity created - Payment successful!")

        initViews()
        setupClickListeners()

        // Auto close after 5 seconds (optional)
        scheduleAutoClose()
    }

    private fun initViews() {
        closeButton = findViewById(R.id.btn_close)
        Log.d(TAG, "✅ Views initialized")
    }

    private fun setupClickListeners() {
        closeButton.setOnClickListener {
            Log.d(TAG, "❌ Close button clicked")
            navigateToHome()
        }

        // Allow clicking anywhere on screen to close (optional)
        findViewById<android.widget.RelativeLayout>(android.R.id.content).setOnClickListener {
            Log.d(TAG, "📱 Screen tapped - closing confirmation")
            navigateToHome()
        }

        Log.d(TAG, "✅ Click listeners setup completed")
    }

    private fun scheduleAutoClose() {
        // Auto close after 5 seconds (you can remove this if not needed)
        closeButton.postDelayed({
            Log.d(TAG, "⏰ Auto closing confirmation screen")
            navigateToHome()
        }, 5000) // 5 seconds
    }

    private fun navigateToHome() {
        Log.d(TAG, "🏠 Navigating back to home")

        // Create intent to go back to MainActivity and clear the task stack
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        // Override back button to navigate to home instead of previous screen
        super.onBackPressed()
        navigateToHome()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "🔄 CheckoutConfirmActivity destroyed")
    }
}