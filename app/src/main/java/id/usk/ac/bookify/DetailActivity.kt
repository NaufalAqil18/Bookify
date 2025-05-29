package id.usk.ac.bookify

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch

class DetailActivity : AppCompatActivity() {

    private lateinit var bookRepository: BookRepository
    private lateinit var bookCover: ImageView
    private lateinit var bookTitle: TextView
    private lateinit var bookAuthor: TextView
    private lateinit var bookCategory: TextView
    private lateinit var bookRating: TextView
    private lateinit var bookPrice: TextView
    private lateinit var bookDescription: TextView
    private lateinit var addToCartButton: Button
    private lateinit var cartButton: ImageView

    private var currentBook: Book? = null

    companion object {
        const val EXTRA_BOOK_ID = "extra_book_id"
        const val TAG = "DetailActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        initViews()
        setupRepository()

        val bookId = intent.getStringExtra(EXTRA_BOOK_ID)
        if (bookId != null) {
            Log.d(TAG, "📖 Opening book detail for ID: $bookId")
            loadBookDetail(bookId)
        } else {
            Log.e(TAG, "❌ Book ID not found in intent")
            showError("Book ID not found")
            finish()
        }
    }

    @SuppressLint("CutPasteId")
    private fun initViews() {
        Log.d(TAG, "🔧 Initializing views...")

        bookCover = findViewById(R.id.book_cover)
        bookTitle = findViewById(R.id.book_title)
        bookAuthor = findViewById(R.id.book_author_value)
        bookCategory = findViewById(R.id.book_category_value)
        bookRating = findViewById(R.id.book_rating_value)
        bookPrice = findViewById(R.id.book_price_value)
        bookDescription = findViewById(R.id.book_description)
        addToCartButton = findViewById(R.id.btn_add_to_cart)

        // Initialize cart button if it exists in the layout
        try {
            cartButton = findViewById(R.id.btn_add_to_cart)
            setupCartButton()
        } catch (e: Exception) {
            Log.d(TAG, "Cart button not found in layout, skipping cart button setup")
        }

        Log.d(TAG, "✅ Views initialized successfully")
    }

    private fun setupRepository() {
        bookRepository = BookRepository()
        Log.d(TAG, "✅ Repository setup completed")
    }

    private fun setupCartButton() {
        cartButton.setOnClickListener {
            Log.d(TAG, "🛒 Cart button clicked")
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadBookDetail(bookId: String) {
        Log.d(TAG, "🔍 Loading book detail for ID: $bookId")

        lifecycleScope.launch {
            try {
                Log.d(TAG, "📡 Fetching book data from repository...")
                val book = bookRepository.getBookById(bookId)

                if (book != null) {
                    Log.d(TAG, "✅ Book data received: ${book.title}")
                    currentBook = book
                    runOnUiThread {
                        displayBookDetail(book)
                    }
                } else {
                    Log.w(TAG, "⚠ Book not found for ID: $bookId")
                    runOnUiThread {
                        showError("Book not found")
                        finish()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error loading book detail for ID: $bookId", e)
                runOnUiThread {
                    showError("Failed to load book details: ${e.message}")
                }
            }
        }
    }

    private fun displayBookDetail(book: Book) {
        Log.d(TAG, "🎨 Displaying book details for: ${book.title}")

        // Set book title
        bookTitle.text = book.title
        Log.d(TAG, "📝 Title set: ${book.title}")

        // Set book author
        bookAuthor.text = book.author
        Log.d(TAG, "👤 Author set: ${book.author}")

        // Set book category
        bookCategory.text = book.category
        Log.d(TAG, "🏷 Category set: ${book.category}")

        // Set book rating
        bookRating.text = "${book.rating}/5"
        Log.d(TAG, "⭐ Rating set: ${book.rating}/5")

        // Set book price
        bookPrice.text = "$${String.format("%.2f", book.price)}"
        Log.d(TAG, "💰 Price set: $${String.format("%.2f", book.price)}")

        // Set book description
        val description = if (book.description.isNotEmpty()) {
            book.description
        } else {
            "No description available"
        }
        bookDescription.text = description
        Log.d(TAG, "📄 Description set")

        // Load book cover image
        loadBookCover(book)

        // Setup add to cart button
        setupAddToCartButton(book)

        // Update add to cart button state
        updateAddToCartButtonState(book)

        Log.d(TAG, "✅ All book details displayed successfully")
    }

    private fun loadBookCover(book: Book) {
        Log.d(TAG, "🖼 Loading book cover...")

        if (!book.imageUrl.isNullOrEmpty()) {
            Log.d(TAG, "📸 Loading image from URL: ${book.imageUrl}")
            Glide.with(this)
                .load(book.imageUrl)
                .placeholder(R.drawable.picture3) // default placeholder
                .error(R.drawable.picture3) // error placeholder
                .centerCrop()
                .into(bookCover)
        } else {
            Log.d(TAG, "📸 No image URL provided, using default placeholder")
            bookCover.setImageResource(R.drawable.picture3)
        }
    }

    private fun setupAddToCartButton(book: Book) {
        Log.d(TAG, "🛒 Setting up add to cart button for: ${book.title}")

        addToCartButton.setOnClickListener {
            addToCart(book)
        }
    }

    private fun updateAddToCartButtonState(book: Book) {
        val isInCart = CartManager.isInCart(book.bookId)

        if (isInCart) {
            addToCartButton.text = "Already in Cart"
            addToCartButton.isEnabled = false
            addToCartButton.alpha = 0.6f
            Log.d(TAG, "🔄 Button state updated: Already in cart")
        } else {
            addToCartButton.text = "Add to Cart"
            addToCartButton.isEnabled = true
            addToCartButton.alpha = 1.0f
            Log.d(TAG, "🔄 Button state updated: Available to add")
        }
    }

    private fun addToCart(book: Book) {
        Log.d(TAG, "🛒 Adding to cart: ${book.title}")

        try {
            // Add book to cart using CartManager
            CartManager.addToCart(book)

            // Show success feedback
            Toast.makeText(
                this,
                "${book.title} added to cart!",
                Toast.LENGTH_SHORT
            ).show()

            // Update button state
            updateAddToCartButtonState(book)

            // Optional: Show cart icon with animation or badge update
            animateCartIcon()

            Log.d(TAG, "✅ Successfully added to cart: ${book.title}")
            Log.d(TAG, "📊 Current cart items: ${CartManager.getCartItemCount()}")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error adding book to cart: ${book.title}", e)
            Toast.makeText(
                this,
                "Failed to add book to cart",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun animateCartIcon() {
        try {
            // Simple scale animation for cart button if it exists
            cartButton.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(100)
                .withEndAction {
                    cartButton.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(100)
                        .start()
                }
                .start()
        } catch (e: Exception) {
            Log.d(TAG, "Cart icon animation skipped (cart button not available)")
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        Log.e(TAG, "❌ Error shown to user: $message")
    }

    override fun onResume() {
        super.onResume()
        // Update button state when returning to this activity
        currentBook?.let { book ->
            updateAddToCartButtonState(book)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "🔄 DetailActivity destroyed")
        }
}
