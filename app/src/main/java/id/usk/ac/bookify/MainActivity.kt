package id.usk.ac.bookify

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
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
        setContentView(R.layout.activity_main)

        initViews()
        setupRecyclerViews()
        setupBottomNavigation()
        loadData()
    }

    private fun initViews() {
        try {
            bestDealsRecyclerView = findViewById(R.id.bestDealsRecyclerView)
            topBooksRecyclerView = findViewById(R.id.topBooksRecyclerView)
            latestBooksRecyclerView = findViewById(R.id.latestBooksRecyclerView)
            bookRepository = BookRepository()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error initializing views", e)
            Toast.makeText(this, "Error initializing app", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupRecyclerViews() {
        try {
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
        } catch (e: Exception) {
            Log.e("MainActivity", "Error setting up RecyclerViews", e)
            Toast.makeText(this, "Error setting up book lists", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadData() {
        lifecycleScope.launch {
            try {
                val bestDeals = bookRepository.getBestDeals()
                val topBooks = bookRepository.getTopBooks()
                val latestBooks = bookRepository.getLatestBooks()

                runOnUiThread {
                    try {
                        bestDealsAdapter.updateBooks(bestDeals)
                        topBooksAdapter.updateBooks(topBooks)
                        latestBooksAdapter.updateBooks(latestBooks)
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error updating adapters", e)
                        Toast.makeText(this@MainActivity, "Error displaying books", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error loading data", e)
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Error loading books: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupBottomNavigation() {
        try {
            findViewById<LinearLayout>(R.id.nav_categories).setOnClickListener {
                startActivity(Intent(this, CategoriesActivity::class.java))
                finish()
            }

            findViewById<LinearLayout>(R.id.nav_cart).setOnClickListener {
                startActivity(Intent(this, CartActivity::class.java))
                finish()
            }

            findViewById<LinearLayout>(R.id.nav_profile).setOnClickListener {
                startActivity(Intent(this, ProfileActivity::class.java))
                finish()
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error setting up navigation", e)
            Toast.makeText(this, "Error setting up navigation", Toast.LENGTH_LONG).show()
        }
    }
}