package id.usk.ac.bookify

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class CheckoutConfirmActivity : AppCompatActivity() {

    private lateinit var closeButton: ImageView
    private var autoCloseScheduled = false

    companion object {
        const val TAG = "CheckoutConfirmActivity"
        private const val AUTO_CLOSE_DELAY = 5000L // 5 seconds
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            setContentView(R.layout.activity_checkout_confirm)
            Log.d(TAG, "🎉 CheckoutConfirmActivity created")

            initViews()
            setupClickListeners()
            scheduleAutoClose()

            // Finish the checkout activity
            finishCheckoutActivity()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing CheckoutConfirmActivity", e)
            navigateToHome()
        }
    }

    private fun initViews() {
        try {
            closeButton = findViewById(R.id.btn_close)
            Log.d(TAG, "✅ Views initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views", e)
            throw e
        }
    }

    private fun setupClickListeners() {
        try {
            closeButton.setOnClickListener {
                Log.d(TAG, "❌ Close button clicked")
                navigateToHome()
            }

            // Allow clicking anywhere on screen to close
            findViewById<androidx.constraintlayout.widget.ConstraintLayout>(android.R.id.content).setOnClickListener {
                Log.d(TAG, "📱 Screen tapped - closing confirmation")
                navigateToHome()
            }

            Log.d(TAG, "✅ Click listeners setup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up click listeners", e)
            navigateToHome()
        }
    }

    private fun scheduleAutoClose() {
        if (autoCloseScheduled) return

        try {
            autoCloseScheduled = true
            closeButton.postDelayed({
                if (!isFinishing && !isDestroyed) {
                    Log.d(TAG, "⏰ Auto closing confirmation screen")
                    navigateToHome()
                }
            }, AUTO_CLOSE_DELAY)
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling auto close", e)
        }
    }

    private fun finishCheckoutActivity() {
        try {
            // Find and finish the checkout activity
            for (activity in (application as? android.app.Application)?.activities() ?: emptyList()) {
                if (activity is CheckoutActivity) {
                    activity.finish()
                    break
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error finishing checkout activity", e)
        }
    }

    private fun navigateToHome() {
        try {
            Log.d(TAG, "🏠 Navigating back to home")

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to home", e)
            finish()
        }
    }

    override fun onBackPressed() {
        navigateToHome()
    }

    override fun onPause() {
        super.onPause()
        closeButton.removeCallbacks(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "🔄 CheckoutConfirmActivity destroyed")
    }
}

// Extension function to get all activities
private fun android.app.Application.activities(): List<android.app.Activity> {
    return try {
        val activityThreadClass = Class.forName("android.app.ActivityThread")
        val activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null)
        val activitiesField = activityThreadClass.getDeclaredField("mActivities")
        activitiesField.isAccessible = true
        
        @Suppress("UNCHECKED_CAST")
        val activities = activitiesField.get(activityThread) as? android.util.ArrayMap<Any, Any>
        
        activities?.values?.mapNotNull {
            val activityRecordClass = it.javaClass
            val activityField = activityRecordClass.getDeclaredField("activity")
            activityField.isAccessible = true
            activityField.get(it) as? android.app.Activity
        } ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }
}