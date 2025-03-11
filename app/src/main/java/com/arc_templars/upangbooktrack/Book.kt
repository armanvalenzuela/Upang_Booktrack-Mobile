package com.arc_templars.upangbooktrack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arc_templars.upangbooktrack.models.Item
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface fetchBookApi {
    @GET("user_fetch_books.php")
    fun getBooks(): Call<List<Item>>
}

class Book : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnFilter: ImageView
    private lateinit var itemAdapter: ItemAdapter

    private var itemList = listOf<Item>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book)

        val profileIcon = findViewById<ImageView>(R.id.profileIcon)
        profileIcon.setOnClickListener { view -> showProfileMenu(view) }

        recyclerView = findViewById(R.id.recyclerView)
        btnFilter = findViewById(R.id.btnFilter)

        recyclerView.layoutManager = GridLayoutManager(this, 2)
        itemAdapter = ItemAdapter(itemList) { item -> openItemDetail(item) }
        recyclerView.adapter = itemAdapter
        recyclerView.addItemDecoration(GridSpacingItemDecoration(2, 30, true))

        btnFilter.setOnClickListener { showFilterDialog() }

        fetchBooks() // Fetch books from API

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.menu_book
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.menu_book -> true
                R.id.menu_uniform -> {
                    startActivity(Intent(this, Uniform::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.menu_bookmark -> {
                    startActivity(Intent(this, Saved::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }
    }

    private fun fetchBooks() {
        val apiService = ApiClient.getRetrofitInstance().create(fetchBookApi::class.java)

        apiService.getBooks().enqueue(object : Callback<List<Item>> {
            override fun onResponse(call: Call<List<Item>>, response: Response<List<Item>>) {
                if (response.isSuccessful) {
                    val books = response.body()
                    if (books != null) {
                        itemList = books
                        itemAdapter.updateData(itemList) // Update RecyclerView
                    }
                } else {
                    Toast.makeText(this@Book, "Failed to fetch books", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Item>>, t: Throwable) {
                Toast.makeText(this@Book, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showFilterDialog() {
        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.filter, null)

        val departmentGroup = view.findViewById<RadioGroup>(R.id.departmentGroup)
        val availabilitySwitch = view.findViewById<Switch>(R.id.availabilitySwitch)
        val applyButton = view.findViewById<Button>(R.id.applyFilterButton)

        applyButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun showProfileMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val studentName = sharedPreferences.getString("studentName", "Guest") ?: "Guest"
        val studentNumber = sharedPreferences.getString("studentNumber", "N/A") ?: "N/A"
        val department = sharedPreferences.getString("department", "Unknown") ?: "Unknown"

        popupMenu.menu.add("$studentName\n$studentNumber\n$department").isEnabled = false
        popupMenu.menu.add("Change Password")
        popupMenu.menu.add("Log Out")

        popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.title) {
                "Change Password" -> startActivity(Intent(this, ChangePassActivity::class.java))
                "Log Out" -> showLogoutConfirmation()
            }
            true
        }
        popupMenu.show()
    }

    private fun showLogoutConfirmation() {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { _, _ ->
                val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.clear()
                editor.apply()

                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton("Cancel", null)
            .create()

        alertDialog.show()
    }

    private fun openItemDetail(item: Item) {
        val intent = Intent(this, ItemDetail::class.java)
        intent.putExtra("itemType", "book")
        intent.putExtra("title", item.name)
        intent.putExtra("description", "${item.category} | ${item.department}")
        intent.putExtra("stock", item.stock)
        intent.putExtra("imageResId", item.imageResId)
        startActivity(intent)
    }
}
