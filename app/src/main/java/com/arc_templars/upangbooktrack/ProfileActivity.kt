package com.arc_templars.upangbooktrack

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Back Button - Return to MainActivity
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish() // Closes the current activity and returns to MainActivity
        }

        // Sign Out Button - Show Confirmation Before Logging Out
        val btnSignOut = findViewById<ImageButton>(R.id.btnSignOut)
        btnSignOut.setOnClickListener {
            showSignOutConfirmation()
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
