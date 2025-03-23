package com.arc_templars.upangbooktrack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.util.Log
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

interface fetchUnifApi{
    @GET("user_fetch_uniforms.php")
    fun getuniform(): Call<List<Item>>
}

class Uniform : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnFilter: ImageView
    private lateinit var itemAdapter: ItemAdapter

    private var selectedCategory: String? = null
    private var showAvailableOnly: Boolean = false
    private var itemList = listOf<Item>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_uniform)

        val profileIcon = findViewById<ImageView>(R.id.profileIcon)

        // Show Dropdown Menu on Profile Icon Click
        profileIcon.setOnClickListener { showProfileMenu() }

        recyclerView = findViewById(R.id.recyclerView)
        btnFilter = findViewById(R.id.btnFilter)

        // Set up RecyclerView with Click Listener
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        itemAdapter = ItemAdapter(itemList) { item -> openItemDetail(item) }
        recyclerView.adapter = itemAdapter
        recyclerView.addItemDecoration(GridSpacingItemDecoration(2, 30, true))

        //Open Filter Dialog on Click
        btnFilter.setOnClickListener { showFilterDialog() }

        fetchUniforms() // Fetch data from API

        //Bottom Navigation - Highlight Uniform
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.menu_uniform
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.menu_book -> {
                    startActivity(Intent(this, Book::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.menu_uniform -> true
                /* R.id.menu_bookmark -> {
                    startActivity(Intent(this, Saved::class.java))
                    overridePendingTransition(0, 0)
                    true
                }*/
                else -> false
            }
        }
    }

    private fun fetchUniforms() {
        val apiService = ApiClient.getRetrofitInstance().create(fetchUnifApi::class.java)

        apiService.getuniform().enqueue(object : Callback<List<Item>> {
            override fun onResponse(call: Call<List<Item>>, response: Response<List<Item>>) {
                if (response.isSuccessful) {
                    val uniforms = response.body()
                    if (uniforms != null) {
                        itemList = uniforms
                        itemAdapter.updateData(itemList) // Update RecyclerView
                    }
                } else {
                    Toast.makeText(this@Uniform, "Failed to fetch uniforms", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Item>>, t: Throwable) {
                Toast.makeText(this@Uniform, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    //Function to Open Filter Dialog
    private fun showFilterDialog() {
        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.filter, null)

        val departmentGroup = view.findViewById<RadioGroup>(R.id.departmentGroup)
        val availabilitySwitch = view.findViewById<Switch>(R.id.availabilitySwitch)
        val applyButton = view.findViewById<Button>(R.id.applyFilterButton)

        //Restore Previous Selection
        when (selectedCategory) {
            "CEA" -> departmentGroup.check(R.id.department_cea)
            "CAS" -> departmentGroup.check(R.id.department_cas)
            "CMA" -> departmentGroup.check(R.id.department_cma)
            "CAHS" -> departmentGroup.check(R.id.department_cahs)
            "CCJE" -> departmentGroup.check(R.id.department_ccje)
            "CITE" -> departmentGroup.check(R.id.department_cite)
        }
        availabilitySwitch.isChecked = showAvailableOnly

        //Set Category Selection
        departmentGroup.setOnCheckedChangeListener { _, checkedId ->
            selectedCategory = when (checkedId) {
                R.id.department_cea -> "CEA"
                R.id.department_cas -> "CAS"
                R.id.department_cma -> "CMA"
                R.id.department_cahs -> "CAHS"
                R.id.department_ccje -> "CCJE"
                R.id.department_cite -> "CITE"
                else -> null
            }
        }

        //Set Availability Toggle
        availabilitySwitch.setOnCheckedChangeListener { _, isChecked ->
            showAvailableOnly = isChecked
        }

        //Apply Filters and Close Dialog
        applyButton.setOnClickListener {
            applyFilters()
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    //Function to Apply Filters
    private fun applyFilters() {
        var filteredList = itemList

        //Filter by Category
        if (selectedCategory != null) {
            filteredList = filteredList.filter { it.department == selectedCategory }
        }

        // filter by Availability
        if (showAvailableOnly) {
            filteredList = filteredList.filter { it.availability }
        }

        //Update the RecyclerView
        itemAdapter.updateData(filteredList)
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
    //  Function to Open Item Details
    private fun openItemDetail(item: Item) {

        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("user_id", -1) // Get user_id, default to -1 if not found
        // Filter all items with the same name, department, AND gender
        val relatedSizes = itemList
            .filter { it.name == item.name && it.department == item.department && it.gender == item.gender }
            .joinToString(" ") { "${it.size}:${it.stock}" }

        val intent = Intent(this, ItemDetail::class.java)
        intent.putExtra("itemType", "uniform")
        intent.putExtra("title", item.name)
        intent.putExtra("description", "${item.category} | ${item.department} | ${item.gender}")
        intent.putExtra("sizes", relatedSizes)
        intent.putExtra("gender", item.gender)
        intent.putExtra("imageResId", item.imageResId)
        intent.putExtra("uniform_id", item.uniform_id) // ✅ Add uniform_id
        intent.putExtra("user_id", userId) // ✅ Pass user ID from session/storage
        startActivity(intent)
    }
}
