package id.usk.ac.bookify

data class Book(
    val bookId: String = "",
    val title: String = "",
    val author: String = "",
    val category: String = "",
    val price: Double = 0.0,
    val rating: Double = 0.0,
    val imageUrl: String = "",
    val description: String = "",
    val isBestDeal: Boolean = false,
    val isTopBook: Boolean = false,
    val isLatestBook: Boolean = false,
    val publishedDate: String = "",
    val stock: Int = 0
)
