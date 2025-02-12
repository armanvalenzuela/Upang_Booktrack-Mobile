package com.arc_templars.upangbooktrack

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Sign Out Button - Show Confirmation Before Logging Out
        val btnSignOut = findViewById<ImageButton>(R.id.btnSignOut)
        btnSignOut.setOnClickListener {
            showSignOutConfirmation()
        }

        // Bottom Navigation
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        // Highlight Profile tab when ProfileActivity is opened
        bottomNavigation.selectedItemId = R.id.menu_profile

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(0, 0) // Removes transition animation
                    finish()
                    true
                }
                R.id.menu_settings -> {
                    true
                }
                R.id.menu_notifications -> {
                    true
                }
                R.id.menu_profile -> true // Stay on the current activity
                else -> false
            }
        }
    }

    private fun showSignOutConfirmation() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Sign Out")
        alertDialog.setMessage("Are you sure you want to sign out?")
        alertDialog.setPositiveButton("Yes") { _, _ ->
            // Navigate back to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
        alertDialog.setNegativeButton("Cancel", null)
        alertDialog.show()
    }
}
