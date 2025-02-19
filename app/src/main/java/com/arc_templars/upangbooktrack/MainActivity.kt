package com.arc_templars.upangbooktrack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arc_templars.upangbooktrack.models.Item
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvGreeting = findViewById<TextView>(R.id.tvGreeting)

        // SHARED PREF RETRIEVAL (ALSO STORED: id, studentNo, password)
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val studentName = sharedPreferences.getString("studentName", "Guest") // Default: "Guest"
        tvGreeting.text = "Welcome, $studentName!"

        val profileIcon = findViewById<ImageView>(R.id.profileIcon) // ✅ Fixed Type

        // ✅ Make Profile Icon Clickable
        profileIcon.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }


        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        // Sample Data
        val itemList = listOf(
            Item("Book 1", R.drawable.book_icon, true),
            Item("Book 2", R.drawable.book_icon, false),
            Item("Uniform 1", R.drawable.uniform_icon, true),
            Item("Uniform 2", R.drawable.uniform_icon, false),
            Item("Module 1", R.drawable.module_icon, true),
            Item("Module 2", R.drawable.module_icon, false),
            Item("Book 3", R.drawable.book_icon, true),
            Item("Book 4", R.drawable.book_icon, false),
            Item("Uniform 3", R.drawable.uniform_icon, true),
            Item("Uniform 4", R.drawable.uniform_icon, false),
            Item("Module 3", R.drawable.module_icon, true),
            Item("Module 4", R.drawable.module_icon, false)
        )

        // Centered GridLayoutManager (2 columns)
        val layoutManager = GridLayoutManager(this, 2)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = ItemAdapter(itemList)

        // Adjust spacing
        recyclerView.addItemDecoration(GridSpacingItemDecoration(2, 30, true))

        // Bottom Navigation
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_book -> true
                R.id.menu_uniform -> true
                R.id.menu_home -> true
                R.id.menu_file -> true
                R.id.menu_bookmark -> true
                else -> false
            }
        }
    }
}
