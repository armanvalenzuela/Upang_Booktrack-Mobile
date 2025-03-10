package com.arc_templars.upangbooktrack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.Switch
import android.widget.TextView
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
        profileIcon.setOnClickListener {
            showProfileMenu()
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
        intent.putExtra("stock", item.stock)
        intent.putExtra("sizes", item.sizes)
        intent.putExtra("imageResId", item.imageResId)

        if (item.category.lowercase() == "book") {
            intent.putExtra("stock", item.stock)
        } else if (item.category.lowercase() == "uniform") {
            intent.putExtra("sizes", item.sizes)
        }
        startActivity(intent)
    }

    // Function to Show Profile Dropdown Menu
    private fun showProfileMenu() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_profile_menu, null)
        builder.setView(dialogView)

        val alertDialog = builder.create()

        // Apply transparent background
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // **Set custom width & height**
        alertDialog.setOnShowListener {
            val window = alertDialog.window
            window?.setLayout(400 * resources.displayMetrics.density.toInt(), 375 * resources.displayMetrics.density.toInt()) // Convert dp to pixels
        }

        // Find views
        val txtUserInfo = dialogView.findViewById<TextView>(R.id.txtUserInfo)
        val btnChangePassword = dialogView.findViewById<LinearLayout>(R.id.btnChangePassword)
        val btnLogout = dialogView.findViewById<LinearLayout>(R.id.btnLogout)

        // Load user details
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val studentName = sharedPreferences.getString("studentName", "Lastname, Firstname") ?: "Lastname, Firstname"
        val studentNumber = sharedPreferences.getString("studentNumber", "N/A") ?: "N/A"

        txtUserInfo.text = "Welcome, $studentName"

        // Button click listeners
        btnChangePassword.setOnClickListener {
            startActivity(Intent(this, ChangePassActivity::class.java))
            alertDialog.dismiss()
        }

        btnLogout.setOnClickListener {
            showLogoutConfirmation()
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    // Function to Show Log Out Confirmation Dialog
    private fun showLogoutConfirmation() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_logout_confirmation, null)
        val dialog = AlertDialog.Builder(this, R.style.CustomDialog)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Apply animation
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation

        // Find views
        val switchRememberMe = dialogView.findViewById<Switch>(R.id.switchRememberMe)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnConfirmLogout = dialogView.findViewById<Button>(R.id.btnConfirmLogout)

        // Cancel button
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        // Confirm Logout button
        btnConfirmLogout.setOnClickListener {
            val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()

            // Check if "Remember Me" is checked
            if (!switchRememberMe.isChecked) {
                editor.clear() // Clear saved login info
            }
            editor.apply()

            // Redirect to LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
            finish()

            dialog.dismiss()
        }

        dialog.show()
    }

}
