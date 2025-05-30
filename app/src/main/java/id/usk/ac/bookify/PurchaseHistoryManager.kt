package id.usk.ac.bookify

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.ktx.Firebase

object PurchaseHistoryManager {
    private val database = Firebase.database
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun savePurchaseHistory(cartItems: List<CartItem>, callback: (Boolean) -> Unit) {
        val currentUser = auth.currentUser ?: return

        // Get user data from Firestore
        firestore.collection("users").document(currentUser.uid)
            .get()
            .addOnSuccessListener { document: DocumentSnapshot ->
                if (document != null && document.exists()) {
                    val username = document.getString("username") ?: ""
                    val phoneNumber = document.getString("phoneNumber") ?: ""

                    // Create purchase history object
                    val purchaseId = database.reference.child("purchases")
                        .child(currentUser.uid).push().key ?: return@addOnSuccessListener

                    val purchasedBooks = cartItems.map { cartItem ->
                        PurchasedBook(
                            bookId = cartItem.book.bookId,
                            title = cartItem.book.title,
                            quantity = cartItem.quantity,
                            pricePerItem = cartItem.book.price
                        )
                    }

                    val totalAmount = cartItems.sumOf { it.getTotalPrice() }

                    val purchaseHistory = PurchaseHistory(
                        purchaseId = purchaseId,
                        userId = currentUser.uid,
                        username = username,
                        phoneNumber = phoneNumber,
                        books = purchasedBooks,
                        totalAmount = totalAmount
                    )

                    // Save to Realtime Database
                    database.reference.child("purchases")
                        .child(currentUser.uid)
                        .child(purchaseId)
                        .setValue(purchaseHistory)
                        .addOnCompleteListener { task ->
                            callback(task.isSuccessful)
                        }
                }
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    fun getUserPurchaseHistory(callback: (List<PurchaseHistory>) -> Unit) {
        val currentUser = auth.currentUser ?: return

        database.reference.child("purchases")
            .child(currentUser.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val purchaseList = mutableListOf<PurchaseHistory>()
                
                snapshot.children.forEach { purchaseSnapshot ->
                    purchaseSnapshot.getValue(PurchaseHistory::class.java)?.let { purchase ->
                        purchaseList.add(purchase)
                    }
                }
                
                callback(purchaseList.sortedByDescending { it.purchaseDate })
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }
} 