package id.usk.ac.bookify

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var bestDealsAdapter: BestDealsAdapter
    private lateinit var topBooksAdapter: BooksAdapter
    private lateinit var latestBooksAdapter: BooksAdapter
    private lateinit var bookRepository: BookRepository

    // RecyclerViews
    private lateinit var bestDealsRecyclerView: RecyclerView
    private lateinit var topBooksRecyclerView: RecyclerView
    private lateinit var latestBooksRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage)

        // Tambahkan test ini SEBELUM initViews()
        testFirebaseDataStructure()

        initViews()
        setupRecyclerViews()
        loadData()
    }

    private fun testFirebaseDataStructure() {
        Log.d("Firebase", "🧪 === TESTING FIREBASE DATA STRUCTURE ===")

        val database = FirebaseDatabase.getInstance()
        val booksRef = database.getReference("books")

        booksRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("Firebase", "✅ Connected to Firebase!")
                Log.d("Firebase", "📊 Books node exists: ${snapshot.exists()}")
                Log.d("Firebase", "📚 Total books: ${snapshot.childrenCount}")

                if (snapshot.exists()) {
                    var bestDealCount = 0
                    var topBookCount = 0
                    var latestBookCount = 0

                    for (child in snapshot.children) {
                        Log.d("Firebase", "")
                        Log.d("Firebase", "📖 Book ID: ${child.key}")

                        // Print raw data untuk debugging
                        val rawData = child.value as? Map<String, Any>
                        Log.d("Firebase", "📄 Raw Data: $rawData")

                        // Parse sebagai Book object
                        val book = child.getValue(Book::class.java)
                        if (book != null) {
                            Log.d("Firebase", "✅ Title: ${book.title}")
                            Log.d("Firebase", "👤 Author: ${book.author}")
                            Log.d("Firebase", "🏷️ Category: ${book.category}")
                            Log.d("Firebase", "💰 Price: ${book.price}")
                            Log.d("Firebase", "⭐ Rating: ${book.rating}")
                            Log.d("Firebase", "🎯 isBestDeal: ${book.isBestDeal} (${book.isBestDeal::class.java.simpleName})")
                            Log.d("Firebase", "🔝 isTopBook: ${book.isTopBook} (${book.isTopBook::class.java.simpleName})")
                            Log.d("Firebase", "🆕 isLatestBook: ${book.isLatestBook} (${book.isLatestBook::class.java.simpleName})")

                            // Count books by category
                            if (book.isBestDeal) bestDealCount++
                            if (book.isTopBook) topBookCount++
                            if (book.isLatestBook) latestBookCount++

                        } else {
                            Log.e("Firebase", "❌ Failed to parse book: ${child.key}")
                            Log.e("Firebase", "❌ Raw value: ${child.value}")
                        }
                    }

                    Log.d("Firebase", "")
                    Log.d("Firebase", "📊 === SUMMARY ===")
                    Log.d("Firebase", "🎯 Best Deals: $bestDealCount books")
                    Log.d("Firebase", "🔝 Top Books: $topBookCount books")
                    Log.d("Firebase", "🆕 Latest Books: $latestBookCount books")

                } else {
                    Log.e("Firebase", "❌ NO DATA FOUND!")
                    Log.e("Firebase", "💡 Please check:")
                    Log.e("Firebase", "   1. Firebase Console has data in 'books' node")
                    Log.e("Firebase", "   2. Internet connection is working")
                    Log.e("Firebase", "   3. Firebase rules allow read access")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "❌ FIREBASE ERROR:")
                Log.e("Firebase", "   Message: ${error.message}")
                Log.e("Firebase", "   Code: ${error.code}")
                Log.e("Firebase", "   Details: ${error.details}")
            }
        })
    }

    private fun initViews() {
        bestDealsRecyclerView = findViewById(R.id.bestDealsRecyclerView)
        topBooksRecyclerView = findViewById(R.id.topBooksRecyclerView)
        latestBooksRecyclerView = findViewById(R.id.latestBooksRecyclerView)

        bookRepository = BookRepository()
    }

    private fun setupRecyclerViews() {
        // Setup Best Deals RecyclerView
        bestDealsAdapter = BestDealsAdapter(emptyList())
        bestDealsRecyclerView.apply {
            adapter = bestDealsAdapter
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
        }

        // Setup Top Books RecyclerView
        topBooksAdapter = BooksAdapter(emptyList())
        topBooksRecyclerView.apply {
            adapter = topBooksAdapter
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
        }

        // Setup Latest Books RecyclerView
        latestBooksAdapter = BooksAdapter(emptyList())
        latestBooksRecyclerView.apply {
            adapter = latestBooksAdapter
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun loadData() {
        Log.d("MainActivity", "=== Starting loadData() ===")

        lifecycleScope.launch {
            try {
                // Test 1: Load Best Deals
                Log.d("MainActivity", "Loading best deals...")
                val bestDeals = bookRepository.getBestDeals()
                Log.d("MainActivity", "Best deals result: ${bestDeals.size} items")

                if (bestDeals.isNotEmpty()) {
                    runOnUiThread {
                        bestDealsAdapter.updateBooks(bestDeals)
                        Log.d("MainActivity", "Best deals adapter updated")
                    }
                } else {
                    Log.w("MainActivity", "No best deals found - check Firebase data")
                }

                // Test 2: Load Top Books
                Log.d("MainActivity", "Loading top books...")
                val topBooks = bookRepository.getTopBooks()
                Log.d("MainActivity", "Top books result: ${topBooks.size} items")

                if (topBooks.isNotEmpty()) {
                    runOnUiThread {
                        topBooksAdapter.updateBooks(topBooks)
                        Log.d("MainActivity", "Top books adapter updated")
                    }
                } else {
                    Log.w("MainActivity", "No top books found - check Firebase data")
                }

                // Test 3: Load Latest Books
                Log.d("MainActivity", "Loading latest books...")
                val latestBooks = bookRepository.getLatestBooks()
                Log.d("MainActivity", "Latest books result: ${latestBooks.size} items")

                if (latestBooks.isNotEmpty()) {
                    runOnUiThread {
                        latestBooksAdapter.updateBooks(latestBooks)
                        Log.d("MainActivity", "Latest books adapter updated")
                    }
                } else {
                    Log.w("MainActivity", "No latest books found - check Firebase data")
                }

                Log.d("MainActivity", "=== loadData() completed ===")

            } catch (e: Exception) {
                Log.e("MainActivity", "=== ERROR in loadData() ===", e)
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}