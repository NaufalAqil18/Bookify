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
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupUI()
        setupBottomNavigation()
    }

    private fun setupUI() {
        val currentUser = auth.currentUser
        val usernameText = findViewById<TextView>(R.id.usernameText)
        val joinedText = findViewById<TextView>(R.id.joinedText)
        
        currentUser?.let { user ->
            db.collection("users")
                .document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Get username
                        val username = document.getString("username") ?: "User"
                        usernameText.text = username

                        // Get and format join date
                        val createdAt = document.getLong("createdAt")
                        if (createdAt != null) {
                            val date = Date(createdAt)
                            val formatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                            joinedText.text = "Joined in ${formatter.format(date)}"
                        }
                    } else {
                        usernameText.text = "User"
                        joinedText.text = "Joined in 2024"
                    }
                }
                .addOnFailureListener { e ->
                    usernameText.text = "User"
                    joinedText.text = "Joined in 2024"
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

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