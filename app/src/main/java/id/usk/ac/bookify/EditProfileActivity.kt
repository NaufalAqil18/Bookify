package id.usk.ac.bookify

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    
    // View bindings
    private lateinit var usernameEdit: EditText
    private lateinit var emailEdit: EditText
    private lateinit var phoneEdit: EditText
    private lateinit var genderSpinner: Spinner
    private lateinit var saveButton: Button
    private lateinit var btnBack: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize views
        initializeViews()
        
        // Setup gender spinner
        setupGenderSpinner()
        
        // Load user data
        loadUserData()
        
        // Setup buttons
        setupButtons()
    }

    private fun initializeViews() {
        try {
            usernameEdit = findViewById(R.id.usernameEdit)
            emailEdit = findViewById(R.id.emailEdit)
            phoneEdit = findViewById(R.id.phoneEdit)
            genderSpinner = findViewById(R.id.genderSpinner)
            saveButton = findViewById(R.id.saveButton)
            btnBack = findViewById(R.id.btnBack)
        } catch (e: Exception) {
            Toast.makeText(this, "Error initializing views: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupGenderSpinner() {
        try {
            ArrayAdapter.createFromResource(
                this,
                R.array.gender_options,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                genderSpinner.adapter = adapter
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error setting up gender spinner: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        try {
                            usernameEdit.setText(document.getString("username"))
                            emailEdit.setText(currentUser.email)
                            phoneEdit.setText(document.getString("phoneNumber"))
                            
                            val gender = document.getString("gender")
                            if (gender != null) {
                                val position = when (gender) {
                                    "Male" -> 1
                                    "Female" -> 2
                                    else -> 0
                                }
                                genderSpinner.setSelection(position)
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this, "Error setting data: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error loading data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun setupButtons() {
        // Back button
        btnBack.setOnClickListener {
            finish()
        }

        // Save button
        saveButton.setOnClickListener {
            saveUserData()
        }
    }

    private fun saveUserData() {
        try {
            val username = usernameEdit.text.toString().trim()
            val newEmail = emailEdit.text.toString().trim()
            val phone = phoneEdit.text.toString().trim()
            val gender = if (genderSpinner.selectedItemPosition > 0) {
                genderSpinner.selectedItem.toString()
            } else {
                null
            }

            // Validate inputs
            if (username.isEmpty() || newEmail.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return
            }

            // Show loading state
            saveButton.isEnabled = false

            // Get current user
            val currentUser = auth.currentUser
            if (currentUser != null) {
                // If email is changed, show re-authentication dialog
                if (newEmail != currentUser.email) {
                    showReAuthDialog(currentUser.email ?: "", newEmail, username, phone, gender)
                } else {
                    // If email is not changed, proceed with normal update
                    updateProfile(username, currentUser.email ?: "", phone, gender)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error saving data: ${e.message}", Toast.LENGTH_SHORT).show()
            saveButton.isEnabled = true
        }
    }

    private fun showReAuthDialog(oldEmail: String, newEmail: String, username: String, phone: String, gender: String?) {
        val builder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.dialog_password_confirm, null)
        val passwordEdit = dialogView.findViewById<EditText>(R.id.passwordEdit)

        builder.setView(dialogView)
            .setTitle("Confirm Password")
            .setMessage("Please enter your password to change email")
            .setPositiveButton("Confirm") { dialog, _ ->
                val password = passwordEdit.text.toString()
                if (password.isNotEmpty()) {
                    reAuthenticateAndUpdateEmail(oldEmail, password, newEmail, username, phone, gender)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                saveButton.isEnabled = true
                dialog.dismiss()
            }
            .show()
    }

    private fun reAuthenticateAndUpdateEmail(oldEmail: String, password: String, newEmail: String, username: String, phone: String, gender: String?) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Show loading
            val loadingDialog = AlertDialog.Builder(this)
                .setMessage("Verifying credentials...")
                .setCancelable(false)
                .create()
            loadingDialog.show()

            val credential = EmailAuthProvider.getCredential(oldEmail, password)
            
            currentUser.reauthenticate(credential)
                .addOnCompleteListener { reAuthTask ->
                    if (reAuthTask.isSuccessful) {
                        // First update other data
                        updateFirestoreData(currentUser.uid, username, oldEmail, phone, gender) { success ->
                            if (success) {
                                // Then send verification email
                                currentUser.verifyBeforeUpdateEmail(newEmail)
                                    .addOnCompleteListener { emailTask ->
                                        loadingDialog.dismiss()
                                        if (emailTask.isSuccessful) {
                                            showSuccessDialog(newEmail)
                                        } else {
                                            val error = emailTask.exception?.message ?: "Unknown error"
                                            showErrorDialog("Failed to send verification email: $error")
                                            saveButton.isEnabled = true
                                        }
                                    }
                            } else {
                                loadingDialog.dismiss()
                                showErrorDialog("Failed to update profile data")
                                saveButton.isEnabled = true
                            }
                        }
                    } else {
                        loadingDialog.dismiss()
                        showErrorDialog("Authentication failed: ${reAuthTask.exception?.message}")
                        saveButton.isEnabled = true
                    }
                }
        }
    }

    private fun showSuccessDialog(newEmail: String) {
        AlertDialog.Builder(this)
            .setTitle("Verification Email Sent")
            .setMessage("We've sent a verification link to $newEmail\n\n" +
                    "Please check your email and click the verification link to complete the email change.\n\n" +
                    "Note: The verification link will expire in 24 hours.")
            .setPositiveButton("OK") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun updateProfile(username: String, email: String, phone: String, gender: String?) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val profileUpdates = userProfileChangeRequest {
                displayName = username
            }

            currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        updateFirestoreData(currentUser.uid, username, email, phone, gender)
                    } else {
                        Toast.makeText(this, "Failed to update profile: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        saveButton.isEnabled = true
                    }
                }
        }
    }

    private fun updateFirestoreData(userId: String, username: String, email: String, phone: String, gender: String?, callback: (Boolean) -> Unit = {}) {
        try {
            val userData = hashMapOf<String, Any>(
                "username" to username,
                "phoneNumber" to phone
            )
            
            gender?.let { userData["gender"] = it }

            db.collection("users").document(userId)
                .update(userData)
                .addOnSuccessListener {
                    if (callback == {}) {
                        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    callback(true)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to update profile: ${e.message}", Toast.LENGTH_SHORT).show()
                    saveButton.isEnabled = true
                    callback(false)
                }
        } catch (e: Exception) {
            Toast.makeText(this, "Error updating Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
            saveButton.isEnabled = true
            callback(false)
        }
    }
} 