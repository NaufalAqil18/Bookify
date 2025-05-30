package id.usk.ac.bookify

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
// Hapus R jika tidak digunakan secara eksplisit di sini, Android Studio akan mengurusnya
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore // Import Firestore
import com.google.firebase.firestore.ktx.firestore // Import Firestore KTX
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {
    private lateinit var etEmail: EditText
    private lateinit var etConfPass: EditText
    private lateinit var etPass: EditText
    private lateinit var btnSignUp: Button
    private lateinit var tvRedirectLogin: TextView
    private lateinit var etUsername: EditText // Tambahkan ini
    private lateinit var etPhoneNumber: EditText // Tambahkan ini

    // create Firebase authentication object
    private lateinit var auth: FirebaseAuth
    // create Firebase Firestore object
    private lateinit var db: FirebaseFirestore // Tambahkan ini


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // View Bindings
        etUsername = findViewById(R.id.etSUsername) // Tambahkan ini
        etPhoneNumber = findViewById(R.id.etSPhoneNumber) // Tambahkan ini
        etEmail = findViewById(R.id.etSEmailAddress)
        etConfPass = findViewById(R.id.etSConfPassword)
        etPass = findViewById(R.id.etSPassword)
        btnSignUp = findViewById(R.id.btnSSigned)
        tvRedirectLogin = findViewById(R.id.tvRedirectLogin)

        // Initialising auth object
        auth = Firebase.auth
        // Initialising Firestore object
        db = Firebase.firestore // Tambahkan ini

        btnSignUp.setOnClickListener {
            signUpUser()
        }

        // switching from signUp Activity to Login Activity
        tvRedirectLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Tambahkan finish agar activity ini ditutup
        }

        // Handle back button click (jika ic_back ada)
        val btnBack: android.widget.ImageButton? = findViewById(R.id.btnBack)
        btnBack?.setOnClickListener {
            finish() // Kembali ke activity sebelumnya
        }
    }

    private fun signUpUser() {
        val username = etUsername.text.toString() // Tambahkan ini
        val phoneNumber = etPhoneNumber.text.toString() // Tambahkan ini
        val email = etEmail.text.toString()
        val pass = etPass.text.toString()
        val confirmPassword = etConfPass.text.toString()

        // check fields
        if (username.isBlank() || phoneNumber.isBlank() || email.isBlank() || pass.isBlank() || confirmPassword.isBlank()) {
            Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        if (pass != confirmPassword) {
            Toast.makeText(this, "Password dan Konfirmasi Password tidak cocok", Toast.LENGTH_SHORT)
                .show()
            return
        }

        // Disable signup button to prevent multiple clicks
        btnSignUp.isEnabled = false

        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    
                    // Update display name
                    val profileUpdates = userProfileChangeRequest {
                        displayName = username
                    }

                    firebaseUser?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { profileTask ->
                            if (profileTask.isSuccessful) {
                                // Save additional user info to Firestore
                                saveUserToFirestore(firebaseUser.uid, username, email, phoneNumber)
                            } else {
                                Toast.makeText(this, "Gagal mengupdate profil: ${profileTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                btnSignUp.isEnabled = true
                            }
                        }
                } else {
                    Toast.makeText(this, "Pendaftaran Gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    btnSignUp.isEnabled = true
                }
            }
    }

    private fun saveUserToFirestore(userId: String, username: String, email: String, phoneNumber: String) {
        val user = hashMapOf(
            "username" to username,
            "email" to email,
            "phoneNumber" to phoneNumber,
            "createdAt" to System.currentTimeMillis()
        )

        db.collection("users")
            .document(userId)
            .set(user)
            .addOnSuccessListener {
                Toast.makeText(this, "Pendaftaran berhasil!", Toast.LENGTH_SHORT).show()
                // Redirect to Login
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menyimpan data: ${e.message}", Toast.LENGTH_SHORT).show()
                btnSignUp.isEnabled = true
            }
    }
}