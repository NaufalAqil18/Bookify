package id.usk.ac.bookify

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity

class CategoriesActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "CategoriesActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)

        setupCategoryClickListeners()
        setupBottomNavigation()
    }

    private fun setupCategoryClickListeners() {
        // Get references to category layouts
        val categoryNonFiction = findViewById<RelativeLayout>(R.id.category_nonfiction)
        val categoryClassics = findViewById<RelativeLayout>(R.id.category_classics)
        val categoryFantasy = findViewById<RelativeLayout>(R.id.category_fantasy)
        val categoryYoungAdult = findViewById<RelativeLayout>(R.id.category_young_adult)
        val categoryCrime = findViewById<RelativeLayout>(R.id.category_crime)
        val categoryHorror = findViewById<RelativeLayout>(R.id.category_horror)
        val categoryScifi = findViewById<RelativeLayout>(R.id.category_scifi)
        val categoryDrama = findViewById<RelativeLayout>(R.id.category_drama)

        // Set click listeners for each category
        categoryNonFiction.setOnClickListener { navigateToCategory("non-fiction", "Non-fiction") }
        categoryClassics.setOnClickListener { navigateToCategory("classics", "Classics") }
        categoryFantasy.setOnClickListener { navigateToCategory("fantasy", "Fantasy") }
        categoryYoungAdult.setOnClickListener { navigateToCategory("young-adult", "Young Adult") }
        categoryCrime.setOnClickListener { navigateToCategory("crime", "Crime") }
        categoryHorror.setOnClickListener { navigateToCategory("horror", "Horror") }
        categoryScifi.setOnClickListener { navigateToCategory("sci-fi", "Sci-fi") }
        categoryDrama.setOnClickListener { navigateToCategory("drama", "Drama") }
    }

    private fun navigateToCategory(categoryId: String, categoryName: String) {
        val intent = Intent(this, CategoryDetailsActivity::class.java).apply {
            putExtra("CATEGORY_ID", categoryId)
            putExtra("CATEGORY_NAME", categoryName)
        }
        startActivity(intent)
    }

    private fun setupBottomNavigation() {
        try {
            // Handle Homepage navigation
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
}