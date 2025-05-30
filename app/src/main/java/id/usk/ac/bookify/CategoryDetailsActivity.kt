package id.usk.ac.bookify

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class CategoryDetailsActivity : AppCompatActivity() {
    private lateinit var bookRepository: BookRepository
    private lateinit var booksAdapter: BooksAdapter
    private lateinit var booksRecyclerView: RecyclerView
    private lateinit var categoryTitle: TextView
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var searchBar: EditText
    private lateinit var btnBack: ImageButton
    
    private var allBooks: List<Book> = emptyList()

    companion object {
        private const val TAG = "CategoryDetailsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_details)

        // Initialize views and repository
        initViews()
        setupRepository()
        setupRecyclerView()
        setupBackButton()
        setupSearchBar()
        setupBottomNavigation()

        // Get category details from intent
        val categoryId = intent.getStringExtra("CATEGORY_ID") ?: ""
        val categoryName = intent.getStringExtra("CATEGORY_NAME") ?: ""

        // Set category title
        categoryTitle.text = categoryName

        // Show loading indicator
        loadingProgressBar.visibility = View.VISIBLE

        // Load books for the category
        loadCategoryBooks(categoryId)
    }

    private fun initViews() {
        booksRecyclerView = findViewById(R.id.booksRecyclerView)
        categoryTitle = findViewById(R.id.categoryNameTextView)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        searchBar = findViewById(R.id.searchBar)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun setupRepository() {
        bookRepository = BookRepository()
    }

    private fun setupRecyclerView() {
        booksAdapter = BooksAdapter(emptyList())
        booksRecyclerView.apply {
            adapter = booksAdapter
            layoutManager = GridLayoutManager(this@CategoryDetailsActivity, 2)
        }
    }

    private fun setupBackButton() {
        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupSearchBar() {
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterBooks(s.toString())
            }
        })
    }

    private fun setupBottomNavigation() {
        try {
            // Handle Home navigation
            findViewById<LinearLayout>(R.id.nav_homepage).setOnClickListener {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }

            // Handle Cart navigation
            findViewById<LinearLayout>(R.id.nav_cart).setOnClickListener {
                startActivity(Intent(this, CartActivity::class.java))
                finish()
            }

            // Handle Profile navigation
            findViewById<LinearLayout>(R.id.nav_profile).setOnClickListener {
                startActivity(Intent(this, ProfileActivity::class.java))
                finish()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up navigation", e)
        }
    }

    private fun loadCategoryBooks(categoryId: String) {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Loading books for category: $categoryId")
                allBooks = bookRepository.getBooksByCategory(categoryId)
                booksAdapter.updateBooks(allBooks)
                Log.d(TAG, "Successfully loaded ${allBooks.size} books")
                loadingProgressBar.visibility = View.GONE
            } catch (e: Exception) {
                Log.e(TAG, "Error loading category books", e)
                loadingProgressBar.visibility = View.GONE
            }
        }
    }

    private fun filterBooks(query: String) {
        if (query.isEmpty()) {
            booksAdapter.updateBooks(allBooks)
            return
        }

        val filteredBooks = allBooks.filter { book ->
            book.title.contains(query, ignoreCase = true) ||
            book.author.contains(query, ignoreCase = true)
        }
        booksAdapter.updateBooks(filteredBooks)
    }
} 