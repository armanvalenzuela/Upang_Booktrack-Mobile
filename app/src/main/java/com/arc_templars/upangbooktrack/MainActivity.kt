package com.arc_templars.upangbooktrack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arc_templars.upangbooktrack.models.Item
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.GET

interface fetchAllApi {
    @GET("user_fetch_all.php")
    fun getAll(): Call<BookUniformResponse>
}

data class BookUniformResponse(
    val books: List<BookData>,
    val uniforms: List<UniformData>
)

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var itemAdapter: ItemAdapter
    private var itemList = listOf<Item>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val profileIcon = findViewById<ImageView>(R.id.profileIcon)
        profileIcon.setOnClickListener { view -> showProfileMenu(view) }

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        itemAdapter = ItemAdapter(itemList) { item -> openItemDetail(item) }
        recyclerView.adapter = itemAdapter
        recyclerView.addItemDecoration(GridSpacingItemDecoration(2, 30, true))

        fetchAllItems()

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.menu_home
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

    private fun fetchAllItems() {
        val apiService = ApiClient.getRetrofitInstance().create(fetchAllApi::class.java)
        apiService.getAll().enqueue(object : Callback<BookUniformResponse> {
            override fun onResponse(call: Call<BookUniformResponse>, response: Response<BookUniformResponse>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        val newList = mutableListOf<Item>()

                        data.books.forEach { book ->
                            newList.add(
                                Item(
                                    name = book.bookname,
                                    imageResId = book.bookimage,
                                    availability = book.bookstat == "available",
                                    category = "Book",
                                    department = book.bookcollege,
                                    description = book.bookdesc,
                                    size = "",
                                    gender = "",
                                    stock = book.bookstock.toIntOrNull() ?: 0
                                )
                            )
                        }

                        data.uniforms.forEach { uniform ->
                            newList.add(
                                Item(
                                    name = uniform.uniformname,
                                    imageResId = uniform.uniformimage,
                                    availability = uniform.uniformstat == "available",
                                    category = "Uniform",
                                    department = uniform.uniformcollege,
                                    description = uniform.uniformdesc,
                                    size = uniform.uniformsize,
                                    gender = uniform.uniformgender,
                                    stock = uniform.uniformstock.toIntOrNull() ?: 0
                                )
                            )
                        }

                        itemList = newList
                        itemAdapter.updateData(itemList)
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Failed to fetch data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BookUniformResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun openItemDetail(item: Item) {
        Toast.makeText(this, "${item.name} - ${item.category}\n${item.department}\nStock: ${item.stock}", Toast.LENGTH_LONG).show()
    }

    private fun showProfileMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val studentName = sharedPreferences.getString("studentName", "Guest") ?: "Guest"
        val studentNumber = sharedPreferences.getString("studentNo", "N/A") ?: "N/A"
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
}
