package id.usk.ac.bookify

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var usernameText: TextView
    private lateinit var joinedText: TextView
    private lateinit var profileImage: CircleImageView
    private lateinit var localBroadcastManager: LocalBroadcastManager

    private val profileUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "PROFILE_UPDATED") {
                loadUserData()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        localBroadcastManager = LocalBroadcastManager.getInstance(this)

        // Initialize views
        usernameText = findViewById(R.id.usernameText)
        joinedText = findViewById(R.id.joinedText)
        profileImage = findViewById(R.id.profileImage)

        setupUI()
        setupBottomNavigation()

        // Register broadcast receiver
        localBroadcastManager.registerReceiver(
            profileUpdateReceiver,
            IntentFilter("PROFILE_UPDATED")
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister broadcast receiver
        localBroadcastManager.unregisterReceiver(profileUpdateReceiver)
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            // Load profile image
            user.photoUrl?.let { uri ->
                Glide.with(this)
                    .load(uri)
                    .into(profileImage)
            }

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
    }

    private fun setupUI() {
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
            showLogoutDialog()
        }
    }

    private fun showLogoutDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_logout)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Set dialog width to match parent with margins
        val width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog.window?.setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT)

        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        val btnConfirm = dialog.findViewById<Button>(R.id.btnConfirm)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
            dialog.dismiss()
        }

        dialog.show()
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