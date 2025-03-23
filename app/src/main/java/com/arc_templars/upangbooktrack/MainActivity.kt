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

        // Show Dropdown Menu on Profile Icon Click
        profileIcon.setOnClickListener { showProfileMenu() }



        // Set up RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        itemAdapter = ItemAdapter(itemList) { item -> openItemDetail(item) }
        recyclerView.adapter = itemAdapter
        recyclerView.addItemDecoration(GridSpacingItemDecoration(2, 30, true))


        // Bottom Navigation
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
                /*R.id.menu_bookmark -> {
                    startActivity(Intent(this, Saved::class.java))
                    overridePendingTransition(0, 0)
                    true
                } */
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
                                    uniform_id = null,
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
                                    uniform_id = uniform.uniform_id,
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

    // Function to Show item detail, fixed for both books and unif -kenchi
    private fun openItemDetail(item: Item) {
        val intent = Intent(this, ItemDetail::class.java)

        if (item.category == "Book") {
            intent.putExtra("itemType", "book")
            intent.putExtra("title", item.name)
            intent.putExtra("description", "${item.category} | ${item.department}")
            intent.putExtra("stock", item.stock)
        } else if (item.category == "Uniform") {
            intent.putExtra("itemType", "uniform")
            intent.putExtra("title", item.name)
            intent.putExtra("description", "${item.category} | ${item.department}")
            intent.putExtra("sizes", "Available Sizes: ${item.size}")
        }

        intent.putExtra("imageResId", item.imageResId)
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

        txtUserInfo.text = "Name: $studentName"

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

            // Redirect to LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
            finish()

            dialog.dismiss()
        }

        dialog.show()
    }
}
