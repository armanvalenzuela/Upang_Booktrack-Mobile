package com.arc_templars.upangbooktrack

import android.content.Context
import android.content.Intent
import android.os.Bundle
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

class Uniform : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnFilter: ImageView
    private lateinit var itemAdapter: ItemAdapter

    private var itemList = listOf(
        Item("Corporate (Male)", R.drawable.bsit_m, true, "Uniform", "CITE"),
        Item("Corporate (Female)", R.drawable.bsit_f, false, "Uniform", "CITE"),
        Item("RSO", R.drawable.tshirt, true, "Uniform", "CITE"),
        Item("Trojan", R.drawable.tshirt, false, "Uniform", "CITE"),
        Item("University (Female)", R.drawable.college_f, true, "Uniform", "General"),
        Item("University (Male)", R.drawable.college_m, true, "Uniform", "General"),
        Item("Nursing (Female)", R.drawable.bspsych_f, true, "Uniform", "CAHS"),
        Item("Nursing (Male)", R.drawable.bspsych_m, true, "Uniform", "CAHS"),
        Item("CPE (Male)", R.drawable.bscomp_m, true, "Uniform", "CEA"),
        Item("CPE (Female)", R.drawable.bscomp_f, true, "Uniform", "CEA"),
        Item("Duty (Male)", R.drawable.bscrim_m, true, "Uniform", "CCJE"),
        Item("Criminology - Duty (Female)", R.drawable.bscrim_f, false, "Uniform", "CCJE"),
        Item("RSO", R.drawable.cas_rso, true, "Uniform", "CAS"),
        Item("RSO", R.drawable.tshirt, false, "Uniform", "CEA"),
    )

    private var selectedCategory: String? = null
    private var showAvailableOnly: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_uniform)

        val profileIcon = findViewById<ImageView>(R.id.profileIcon)

        // Show Dropdown Menu on Profile Icon Click
        profileIcon.setOnClickListener { view ->
            showProfileMenu(view)
        }

        recyclerView = findViewById(R.id.recyclerView)
        btnFilter = findViewById(R.id.btnFilter)

        // ✅ Set up RecyclerView with Click Listener
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        itemAdapter = ItemAdapter(itemList) { item -> openItemDetail(item) }
        recyclerView.adapter = itemAdapter
        recyclerView.addItemDecoration(GridSpacingItemDecoration(2, 30, true))

        // ✅ Open Filter Dialog on Click
        btnFilter.setOnClickListener {
            showFilterDialog()
        }

        // ✅ Bottom Navigation - Highlight Uniform
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
                R.id.menu_bookmark -> {
                    startActivity(Intent(this, Saved::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }
    }

    // ✅ Function to Open Filter Dialog
    private fun showFilterDialog() {
        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.filter, null)

        val departmentGroup = view.findViewById<RadioGroup>(R.id.departmentGroup)
        val availabilitySwitch = view.findViewById<Switch>(R.id.availabilitySwitch)
        val applyButton = view.findViewById<Button>(R.id.applyFilterButton)

        // ✅ Restore Previous Selection
        when (selectedCategory) {
            "CEA" -> departmentGroup.check(R.id.department_cea)
            "CAS" -> departmentGroup.check(R.id.department_cas)
            "CMA" -> departmentGroup.check(R.id.department_cma)
            "CAHS" -> departmentGroup.check(R.id.department_cahs)
            "CCJE" -> departmentGroup.check(R.id.department_ccje)
            "CITE" -> departmentGroup.check(R.id.department_cite)
        }
        availabilitySwitch.isChecked = showAvailableOnly

        // ✅ Set Category Selection
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

        // ✅ Set Availability Toggle
        availabilitySwitch.setOnCheckedChangeListener { _, isChecked ->
            showAvailableOnly = isChecked
        }

        // ✅ Apply Filters and Close Dialog
        applyButton.setOnClickListener {
            applyFilters()
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    // ✅ Function to Apply Filters
    private fun applyFilters() {
        var filteredList = itemList

        // ✅ Filter by Category
        if (selectedCategory != null) {
            filteredList = filteredList.filter { it.department == selectedCategory }
        }

        // ✅ Filter by Availability
        if (showAvailableOnly) {
            filteredList = filteredList.filter { it.availability }
        }

        // ✅ Update the RecyclerView
        itemAdapter.updateData(filteredList)
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

    // ✅ Function to Open Item Details
    private fun openItemDetail(item: Item) {
        val intent = Intent(this, ItemDetail::class.java)
        intent.putExtra("itemType", "uniform")
        intent.putExtra("title", item.name)
        intent.putExtra("description", "${item.category} | ${item.department}")
        intent.putExtra("sizes", "Available Sizes: ${item.sizes}")
        intent.putExtra("imageResId", item.imageResId)
        startActivity(intent)
    }
}
