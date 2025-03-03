package com.arc_templars.upangbooktrack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arc_templars.upangbooktrack.models.Item
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val profileIcon = findViewById<ImageView>(R.id.profileIcon)

        // Show Dropdown Menu on Profile Icon Click
        profileIcon.setOnClickListener { view ->
            showProfileMenu(view)
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        // Items
        val itemList = listOf(
            Item("Applied Anatomy and Physiology", R.drawable.anatomy_physiology, true, "Book", "CAHS"),
            Item("Foundation of Nursing Theories", R.drawable.theories, false, "Book", "CAHS"),
            Item("Corporate (Male)", R.drawable.bsit_m, true, "Uniform", "CITE"),
            Item("Corporate (Female)", R.drawable.bsit_f, false, "Uniform", "CITE"),
            Item("Intermediate Accounting", R.drawable.accounting, true, "Book", "CMA"),
            Item("Auditing and Assurance Services: An Applied Approach", R.drawable.auditing, false, "Book", "CMA"),
            Item("University (Female)", R.drawable.college_f, true, "Uniform", "General"),
            Item("University (Male)", R.drawable.college_m, true, "Uniform", "General"),
            Item("Nursing (Female)", R.drawable.bspsych_f, true, "Uniform", "CAHS"),
            Item("Nursing (Male)", R.drawable.bspsych_m, true, "Uniform", "CAHS"),
            Item("Computer Engineering (Male)", R.drawable.bscomp_m, true, "Uniform", "CEA"),
            Item("Computer Engineering (Female)", R.drawable.bscomp_f, true, "Uniform", "CEA"),
            Item("Criminology - Duty (Male)", R.drawable.bscrim_m, true, "Uniform", "CCJE"),
            Item("Criminology - Duty (Female)", R.drawable.bscrim_f, false, "Uniform", "CCJE")
        )
        // Set up RecyclerView
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        val adapter = ItemAdapter(itemList) { item -> openItemDetail(item) }
        recyclerView.adapter = adapter

        recyclerView.addItemDecoration(GridSpacingItemDecoration(2, 30, true))

        // Bottom Navigation
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_book -> {
                    startActivity(Intent(this, Book::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.menu_uniform -> {
                    startActivity(Intent(this, Uniform::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.menu_home -> true
                R.id.menu_bookmark -> {
                    startActivity(Intent(this, Saved::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }
    }

    // **✅ Function to Open Item Detail Page**
    private fun openItemDetail(item: Item) {
        val intent = Intent(this, ItemDetail::class.java)
        intent.putExtra("itemType", item.category.lowercase()) // "book" or "uniform"
        intent.putExtra("title", item.name)
        intent.putExtra("description", "${item.category} | ${item.department}")
        intent.putExtra("imageResId", item.imageResId)

        if (item.category.lowercase() == "book") {
            intent.putExtra("stock", item.stock)
        } else if (item.category.lowercase() == "uniform") {
            intent.putExtra("sizes", item.sizes)
        }

        startActivity(intent)
    }

    //  Function to Show Profile Dropdown Menu
    private fun showProfileMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        // Retrieve stored user details
        val studentName = sharedPreferences.getString("studentName", "Guest") ?: "Guest"
        val studentNumber = sharedPreferences.getString("studentNumber", "N/A") ?: "N/A"
        val department = sharedPreferences.getString("department", "Unknown") ?: "Unknown"

        // Add header as user info (non-clickable)
        popupMenu.menu.add("$studentName\n$studentNumber\n$department").isEnabled = false

        // Add menu items
        popupMenu.menu.add("Change Password")
        popupMenu.menu.add("Log Out")

        popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.title) {
                "Change Password" -> {
                    startActivity(Intent(this, ChangePassActivity::class.java))
                }
                "Log Out" -> {
                    showLogoutConfirmation() // ✅ Show confirmation before logging out
                }
            }
            true
        }

        popupMenu.show()
    }

    // Function to Show Log Out Confirmation Dialog
    private fun showLogoutConfirmation() {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { _, _ ->
                val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.clear() // Clear saved user data
                editor.apply()

                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton("Cancel", null)
            .create()

        alertDialog.show()
    }
}
