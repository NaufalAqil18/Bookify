package id.usk.ac.bookify

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

class CheckoutConfirmActivity : AppCompatActivity() {

    private lateinit var closeButton: ImageView
    private var autoCloseScheduled = false

    companion object {
        const val TAG = "CheckoutConfirmActivity"
        private const val AUTO_CLOSE_DELAY = 5000L // 5 detik
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            setContentView(R.layout.activity_checkout_confirm)
            Log.d(TAG, "🎉 CheckoutConfirmActivity dibuat")

            initViews()
            setupClickListeners()
            scheduleAutoClose()
        } catch (e: Exception) {
            Log.e(TAG, "Error saat inisialisasi CheckoutConfirmActivity", e)
            navigateToHome()
        }
    }

    private fun initViews() {
        try {
            closeButton = findViewById(R.id.btn_close)
            Log.d(TAG, "✅ Views diinisialisasi")
        } catch (e: Exception) {
            Log.e(TAG, "Error saat inisialisasi views", e)
            throw e
        }
    }

    private fun setupClickListeners() {
        try {
            closeButton.setOnClickListener {
                Log.d(TAG, "❌ Tombol tutup diklik")
                navigateToHome()
            }

            // Memungkinkan klik di mana saja untuk menutup
            findViewById<ConstraintLayout>(R.id.root_layout).setOnClickListener {
                Log.d(TAG, "📱 Layar diketuk - menutup konfirmasi")
                navigateToHome()
            }

            Log.d(TAG, "✅ Click listeners berhasil diatur")
        } catch (e: Exception) {
            Log.e(TAG, "Error saat mengatur click listeners", e)
            navigateToHome()
        }
    }

    private fun scheduleAutoClose() {
        if (autoCloseScheduled) return

        try {
            autoCloseScheduled = true
            closeButton.postDelayed({
                if (!isFinishing && !isDestroyed) {
                    Log.d(TAG, "⏰ Otomatis menutup layar konfirmasi")
                    navigateToHome()
                }
            }, AUTO_CLOSE_DELAY)
        } catch (e: Exception) {
            Log.e(TAG, "Error saat mengatur auto close", e)
        }
    }

    private fun navigateToHome() {
        try {
            Log.d(TAG, "🏠 Kembali ke halaman utama")

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                          Intent.FLAG_ACTIVITY_CLEAR_TOP or 
                          Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Error saat navigasi ke home", e)
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
        Log.d(TAG, "🔄 CheckoutConfirmActivity dihancurkan")
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