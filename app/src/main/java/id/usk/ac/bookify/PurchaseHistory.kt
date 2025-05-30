package id.usk.ac.bookify

data class PurchaseHistory(
    val purchaseId: String = "",
    val userId: String = "",
    val username: String = "",
    val phoneNumber: String = "",
    val books: List<PurchasedBook> = listOf(),
    val totalAmount: Double = 0.0,
    val purchaseDate: Long = System.currentTimeMillis()
)

data class PurchasedBook(
    val bookId: String = "",
    val title: String = "",
    val quantity: Int = 0,
    val pricePerItem: Double = 0.0
) 