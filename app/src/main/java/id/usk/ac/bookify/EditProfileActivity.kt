package id.usk.ac.bookify

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class EditProfileActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    
    // View bindings
    private lateinit var usernameEdit: EditText
    private lateinit var emailEdit: EditText
    private lateinit var phoneEdit: EditText
    private lateinit var genderSpinner: Spinner
    private lateinit var saveButton: Button
    private lateinit var btnBack: ImageButton
    private lateinit var profileImage: CircleImageView
    private lateinit var btnChangePicture: ImageButton
    private lateinit var btnChangePassword: ImageButton

    private var selectedImageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

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
            profileImage = findViewById(R.id.profileImage)
            btnChangePicture = findViewById(R.id.btnChangePicture)
            btnChangePassword = findViewById(R.id.btnChangePassword)
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
            // Load profile image
            currentUser.photoUrl?.let { uri ->
                Glide.with(this)
                    .load(uri)
                    .into(profileImage)
            }

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
        btnBack.setOnClickListener {
            finish()
        }

        saveButton.setOnClickListener {
            saveUserData()
        }

        btnChangePicture.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
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
            // Show loading dialog
            val loadingDialog = AlertDialog.Builder(this)
                .setMessage("Updating profile...")
                .setCancelable(false)
                .create()
            loadingDialog.show()

            val profileUpdates = userProfileChangeRequest {
                displayName = username
            }

            currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        updateFirestoreData(currentUser.uid, username, email, phone, gender)
                        loadingDialog.dismiss()
                    } else {
                        loadingDialog.dismiss()
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
                    // Send broadcast to refresh profile using LocalBroadcastManager
                    LocalBroadcastManager.getInstance(this)
                        .sendBroadcast(Intent("PROFILE_UPDATED"))
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    
                    // Re-enable button and finish activity
                    saveButton.isEnabled = true
                    callback(true)
                    finish()
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

    private fun showChangePasswordDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.dialog_change_password, null)
        
        val currentPasswordEdit = dialogView.findViewById<EditText>(R.id.currentPasswordEdit)
        val newPasswordEdit = dialogView.findViewById<EditText>(R.id.newPasswordEdit)
        val confirmPasswordEdit = dialogView.findViewById<EditText>(R.id.confirmPasswordEdit)

        builder.setView(dialogView)
            .setTitle("Change Password")
            .setPositiveButton("Change") { dialog, _ ->
                val currentPassword = currentPasswordEdit.text.toString()
                val newPassword = newPasswordEdit.text.toString()
                val confirmPassword = confirmPasswordEdit.text.toString()

                if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (newPassword != confirmPassword) {
                    Toast.makeText(this, "New passwords don't match", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (newPassword.length < 6) {
                    Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                changePassword(currentPassword, newPassword)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun changePassword(currentPassword: String, newPassword: String) {
        val user = auth.currentUser
        if (user != null && user.email != null) {
            val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
            
            // Show loading dialog
            val loadingDialog = AlertDialog.Builder(this)
                .setMessage("Changing password...")
                .setCancelable(false)
                .create()
            loadingDialog.show()

            user.reauthenticate(credential)
                .addOnCompleteListener { reAuthTask ->
                    if (reAuthTask.isSuccessful) {
                        user.updatePassword(newPassword)
                            .addOnCompleteListener { task ->
                                loadingDialog.dismiss()
                                if (task.isSuccessful) {
                                    Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this, "Failed to update password: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        loadingDialog.dismiss()
                        Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            uploadImage()
        }
    }

    private fun uploadImage() {
        if (selectedImageUri == null) return

        val loadingDialog = AlertDialog.Builder(this)
            .setMessage("Uploading image...")
            .setCancelable(false)
            .create()
        loadingDialog.show()

        val user = auth.currentUser
        if (user != null) {
            val imageRef = storage.reference.child("profile_images/${user.uid}")
            
            imageRef.putFile(selectedImageUri!!)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        // Update profile photo URL
                        val profileUpdates = userProfileChangeRequest {
                            photoUri = uri
                        }

                        user.updateProfile(profileUpdates)
                            .addOnCompleteListener { task ->
                                loadingDialog.dismiss()
                                if (task.isSuccessful) {
                                    // Update image in UI
                                    Glide.with(this)
                                        .load(uri)
                                        .into(profileImage)
                                    Toast.makeText(this, "Profile picture updated successfully", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this, "Failed to update profile picture", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                }
                .addOnFailureListener { e ->
                    loadingDialog.dismiss()
                    Toast.makeText(this, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
} 