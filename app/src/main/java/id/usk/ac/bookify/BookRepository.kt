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
                    Log.d("BookRepository", "✅ Best deal found: ${book.title}")
                    books.add(book)
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
                    Log.d("BookRepository", "✅ Top book found: ${it.title}")
                    books.add(it)
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
                    Log.d("BookRepository", "✅ Latest book found: ${it.title}")
                    books.add(it)
                }
            }

            books
        } catch (e: Exception) {
            Log.e("BookRepository", "❌ Error fetching latest books", e)
            emptyList()
        }
    }
}