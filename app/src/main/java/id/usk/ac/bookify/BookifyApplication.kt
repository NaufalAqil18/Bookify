package id.usk.ac.bookify

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class BookifyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Enable Firebase offline persistence
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
} 