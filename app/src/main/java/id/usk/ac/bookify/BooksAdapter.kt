package id.usk.ac.bookify

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class BooksAdapter(private var books: List<Book>) : RecyclerView.Adapter<BooksAdapter.ViewHolder>() {

    companion object {
        private const val TAG = "BooksAdapter"
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val bookImage: ImageView = view.findViewById(R.id.bookImage)
        val bookCategory: TextView = view.findViewById(R.id.bookCategory)
        val bookTitle: TextView = view.findViewById(R.id.bookTitle)
        val bookAuthor: TextView = view.findViewById(R.id.bookAuthor)
        val bookPrice: TextView = view.findViewById(R.id.bookPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val book = books[position]

        holder.bookCategory.text = book.category
        holder.bookTitle.text = book.title
        holder.bookAuthor.text = book.author
        holder.bookPrice.text = "$${book.price}"

        // Load image with Glide
        Glide.with(holder.itemView.context)
            .load(book.imageUrl)
            .placeholder(R.drawable.picture3)
            .error(R.drawable.picture3)
            .into(holder.bookImage)

        // Set click listener to open DetailActivity
        holder.itemView.setOnClickListener {
            Log.d(TAG, "📖 Book item clicked: ${book.title} (ID: ${book.bookId})")

            if (book.bookId.isNotEmpty()) {
                val context = holder.itemView.context
                val intent = Intent(context, DetailActivity::class.java).apply {
                    putExtra(DetailActivity.EXTRA_BOOK_ID, book.bookId)
                }
                context.startActivity(intent)
                Log.d(TAG, "✅ Opening DetailActivity for book: ${book.title}")
            } else {
                Log.e(TAG, "❌ Book ID is empty for: ${book.title}")
            }
        }
    }

    override fun getItemCount() = books.size

    fun updateBooks(newBooks: List<Book>) {
        books = newBooks
        notifyDataSetChanged()
        Log.d(TAG, "📊 Books adapter updated with ${newBooks.size} items")
    }
}