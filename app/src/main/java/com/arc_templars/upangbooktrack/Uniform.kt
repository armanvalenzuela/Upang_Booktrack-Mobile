//TODO: add bookmark functionality

package com.arc_templars.upangbooktrack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
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
    private lateinit var etSearchBar: EditText
    private var selectedCategory: String? = null
    private var selectedAvailability: String = "All"
    private var itemList = listOf<Item>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_uniform)

        // Show Dropdown Menu on Profile Icon Click
        val profileIcon = findViewById<ImageView>(R.id.profileIcon)
        profileIcon.setOnClickListener { showProfileMenu() }

        //NOTIFICATION CLICK LISTENER
        val notificationIcon = findViewById<View>(R.id.notificationIcon)
        notificationIcon.setOnClickListener {
            val bottomSheet = UserNotifications()
            bottomSheet.show(supportFragmentManager, bottomSheet.tag)
        }

        recyclerView = findViewById(R.id.recyclerView)
        btnFilter = findViewById(R.id.btnFilter)

        // Set up RecyclerView with Click Listener
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        itemAdapter = ItemAdapter(itemList) { item -> openItemDetail(item) }
        recyclerView.adapter = itemAdapter
        recyclerView.addItemDecoration(GridSpacingItemDecoration(2, 30, true))

        //Open Filter Dialog on Click
        btnFilter.setOnClickListener { showFilterDialog() }

        //Search Bar Implementation
        etSearchBar = findViewById(R.id.etsearchBar)
        etSearchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterItems(s.toString())  // Calls the filtering function
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        fetchUniforms() // Fetch data from API

        // Bottom Navigation - Highlight Uniform
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.menu_uniform
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.menu_book -> {
                    val intent = Intent(this, Book::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.menu_uniform -> true
                R.id.menu_bookmark -> {
                    val intent = Intent(this, Saved::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }

    }

    private fun filterItems(query: String){
        val filteredList = itemList.filter {
            it.name.contains(query, ignoreCase = true)
        }
        itemAdapter.updateData(filteredList)
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
        val availabilitySpinner = view.findViewById<Spinner>(R.id.availabilitySpinner)
        val applyButton = view.findViewById<Button>(R.id.applyFilterButton)

        // Hide SHS, CAS, and CITE Departments
        listOf(R.id.department_shs, R.id.department_cas, R.id.department_cite).forEach { id ->
            val department = view.findViewById<RadioButton>(id)
            department.visibility = View.GONE
            departmentGroup.removeView(department)
        }

        // Restore Previous Department Selection
        when (selectedCategory) {
            "CEA" -> departmentGroup.check(R.id.department_cea)
            "CMA" -> departmentGroup.check(R.id.department_cma)
            "CAHS" -> departmentGroup.check(R.id.department_cahs)
            "CCJE" -> departmentGroup.check(R.id.department_ccje)
        }

        // Restore Previous Availability Selection
        val availabilityOptions = arrayOf("All", "Available", "Not Available")
        val adapter = ArrayAdapter(view.context, android.R.layout.simple_spinner_dropdown_item, availabilityOptions)
        availabilitySpinner.adapter = adapter
        availabilitySpinner.setSelection(availabilityOptions.indexOf(selectedAvailability))

        // ✅ Set Department Selection
        departmentGroup.setOnCheckedChangeListener { _, checkedId ->
            selectedCategory = when (checkedId) {
                R.id.department_cea -> "CEA"
                R.id.department_cma -> "CMA"
                R.id.department_cahs -> "CAHS"
                R.id.department_ccje -> "CCJE"
                else -> null
            }
        }

        // ✅ Set Availability Selection
        availabilitySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedAvailability = availabilityOptions[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // ✅ Apply Filters and Close Dialog
        applyButton.setOnClickListener {
            applyFilters()
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    // Function to Apply Filters
    private fun applyFilters() {
        var filteredList = itemList

        // Filter by Department
        if (selectedCategory != null) {
            filteredList = filteredList.filter { it.department == selectedCategory }
        }

        // Filter by Availability
        when (selectedAvailability) {
            "Available" -> filteredList = filteredList.filter { it.availability }
            "Not Available" -> filteredList = filteredList.filter { !it.availability }
        }

        // Update the RecyclerView
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

            // Completely remove all stored user data
            editor.clear()
            editor.apply()

            // Redirect to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Clears all previous activities
            startActivity(intent)
            finish() // Close MainActivity

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
        intent.putExtra("item_description", item.description)
        intent.putExtra("sizes", relatedSizes)
        intent.putExtra("gender", item.gender)
        intent.putExtra("imageResId", item.imageResId)
        intent.putExtra("uniform_id", item.uniform_id) // ✅ Add uniform_id
        intent.putExtra("user_id", userId) // ✅ Pass user ID from session/storage
        intent.putExtra("availability", item.availability)
        startActivity(intent)
    }

    private var backPressedTime: Long = 0

    override fun onBackPressed() {
        if (backPressedTime + 5000 > System.currentTimeMillis()) {
            super.onBackPressed()
            finishAffinity() // Closes the app
        } else {
            Toast.makeText(this, "Press back again to exit the app", Toast.LENGTH_SHORT).show()
        }
        backPressedTime = System.currentTimeMillis()
    }
}
