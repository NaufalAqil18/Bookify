package id.usk.ac.bookify

data class CartItem(
    val book: Book,
    var quantity: Int = 1
) {
    fun getTotalPrice(): Double {
        return book.price * quantity
    }
}
