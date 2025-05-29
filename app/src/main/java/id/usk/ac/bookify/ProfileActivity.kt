package id.usk.ac.bookify

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()

        setupUI()
        setupBottomNavigation()
    }

    private fun setupUI() {
        // Set username from Firebase
        val currentUser = auth.currentUser
        val usernameText = findViewById<TextView>(R.id.usernameText)
        usernameText.text = currentUser?.displayName ?: "User"

        // Edit Profile button
        findViewById<CardView>(R.id.editProfileButton).setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        // Notification switch
        findViewById<SwitchCompat>(R.id.notificationSwitch).setOnCheckedChangeListener { _, isChecked ->
            val message = if (isChecked) "Notification Allowed" else "Notification Not Allowed"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        // Logout button
        findViewById<CardView>(R.id.logoutButton).setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<LinearLayout>(R.id.bottom_navigation)
        
        bottomNav.findViewById<LinearLayout>(R.id.nav_homepage).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        bottomNav.findViewById<LinearLayout>(R.id.nav_categories).setOnClickListener {
            startActivity(Intent(this, CategoriesActivity::class.java))
            finish()
        }

        bottomNav.findViewById<LinearLayout>(R.id.nav_cart).setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
            finish()
        }
    }
} 