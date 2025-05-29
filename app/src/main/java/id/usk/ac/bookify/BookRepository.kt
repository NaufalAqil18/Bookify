package id.usk.ac.bookify

import android.util.Log
import com.google.firebase.database.*
import kotlinx.coroutines.tasks.await

class BookRepository {
    private val database = FirebaseDatabase.getInstance()
    private val booksRef = database.getReference("books")

    suspend fun getBestDeals(): List<Book> {
        return try {
            Log.d("BookRepository", "🔍 Querying best deals with index...")

            // Sekarang query ini akan bekerja karena sudah ada index
            val snapshot = booksRef.orderByChild("isBestDeal").equalTo(true).get().await()

            Log.d("BookRepository", "📊 Best deals snapshot exists: ${snapshot.exists()}")
            Log.d("BookRepository", "📚 Best deals children count: ${snapshot.childrenCount}")

            val books = mutableListOf<Book>()

            for (childSnapshot in snapshot.children) {
                Log.d("BookRepository", "🔍 Processing best deal: ${childSnapshot.key}")
                val book = childSnapshot.getValue(Book::class.java)
                if (book != null) {
                    // Set bookId from Firebase key if not already set
                    val bookWithId = book.copy(bookId = childSnapshot.key ?: book.bookId)
                    Log.d("BookRepository", "✅ Best deal found: ${bookWithId.title} (ID: ${bookWithId.bookId})")
                    books.add(bookWithId)
                }
            }

            Log.d("BookRepository", "📊 Total best deals loaded: ${books.size}")
            books
        } catch (e: Exception) {
            Log.e("BookRepository", "❌ Error fetching best deals", e)
            emptyList()
        }
    }

    suspend fun getTopBooks(): List<Book> {
        return try {
            Log.d("BookRepository", "🔍 Querying top books with index...")
            val snapshot = booksRef.orderByChild("isTopBook").equalTo(true).get().await()

            Log.d("BookRepository", "📊 Top books snapshot exists: ${snapshot.exists()}")
            Log.d("BookRepository", "📚 Top books children count: ${snapshot.childrenCount}")

            val books = mutableListOf<Book>()

            for (childSnapshot in snapshot.children) {
                val book = childSnapshot.getValue(Book::class.java)
                book?.let {
                    // Set bookId from Firebase key if not already set
                    val bookWithId = it.copy(bookId = childSnapshot.key ?: it.bookId)
                    Log.d("BookRepository", "✅ Top book found: ${bookWithId.title} (ID: ${bookWithId.bookId})")
                    books.add(bookWithId)
                }
            }

            books
        } catch (e: Exception) {
            Log.e("BookRepository", "❌ Error fetching top books", e)
            emptyList()
        }
    }

    suspend fun getLatestBooks(): List<Book> {
        return try {
            Log.d("BookRepository", "🔍 Querying latest books with index...")
            val snapshot = booksRef.orderByChild("isLatestBook").equalTo(true).get().await()

            Log.d("BookRepository", "📊 Latest books snapshot exists: ${snapshot.exists()}")
            Log.d("BookRepository", "📚 Latest books children count: ${snapshot.childrenCount}")

            val books = mutableListOf<Book>()

            for (childSnapshot in snapshot.children) {
                val book = childSnapshot.getValue(Book::class.java)
                book?.let {
                    // Set bookId from Firebase key if not already set
                    val bookWithId = it.copy(bookId = childSnapshot.key ?: it.bookId)
                    Log.d("BookRepository", "✅ Latest book found: ${bookWithId.title} (ID: ${bookWithId.bookId})")
                    books.add(bookWithId)
                }
            }

            books
        } catch (e: Exception) {
            Log.e("BookRepository", "❌ Error fetching latest books", e)
            emptyList()
        }
    }

    // New method to get book by ID
    suspend fun getBookById(bookId: String): Book? {
        return try {
            Log.d("BookRepository", "🔍 Fetching book by ID: $bookId")

            val snapshot = booksRef.child(bookId).get().await()

            if (snapshot.exists()) {
                val book = snapshot.getValue(Book::class.java)
                if (book != null) {
                    // Ensure bookId is set
                    val bookWithId = book.copy(bookId = bookId)
                    Log.d("BookRepository", "✅ Book found: ${bookWithId.title}")
                    return bookWithId
                } else {
                    Log.w("BookRepository", "⚠️ Failed to parse book data for ID: $bookId")
                }
            } else {
                Log.w("BookRepository", "⚠️ No book found with ID: $bookId")
            }

            null
        } catch (e: Exception) {
            Log.e("BookRepository", "❌ Error fetching book by ID: $bookId", e)
            null
        }
    }
}