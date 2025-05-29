package id.usk.ac.bookify

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        findViewById<LinearLayout>(R.id.nav_homepage).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        findViewById<LinearLayout>(R.id.nav_categories).setOnClickListener {
            startActivity(Intent(this, CategoriesActivity::class.java))
            finish()
        }

        findViewById<LinearLayout>(R.id.nav_cart).setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
            finish()
        }
    }
} 