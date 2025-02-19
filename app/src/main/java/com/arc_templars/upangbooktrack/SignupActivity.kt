package com.arc_templars.upangbooktrack

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val btnSignUp = findViewById<Button>(R.id.btnSignUp)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)
        val spinnerDep = findViewById<Spinner>(R.id.spinnerDep)

// Create a list with "Select" as the first item
        val departmentList = mutableListOf("Select a Department")
        departmentList.addAll(resources.getStringArray(R.array.departments))

// Create an adapter with the custom list
        val departmentAdapter = object : ArrayAdapter<String>(
            this, android.R.layout.simple_spinner_item, departmentList
        ) {
            override fun isEnabled(position: Int): Boolean {
                return position != 0  // Disable "Select a Department"
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                if (position == 0) {
                    (view as TextView).setTextColor(Color.GRAY) // Make "Select" look disabled
                } else {
                    (view as TextView).setTextColor(Color.BLACK)
                }
                return view
            }
        }

// Set dropdown style
        departmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDep.adapter = departmentAdapter

// Prevent selection of "Select a Department"
        spinnerDep.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    (view as? TextView)?.setTextColor(Color.GRAY) // Keep "Select" gray in the dropdown
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }



        // Sign Up Button - Placeholder
        btnSignUp.setOnClickListener {
            Toast.makeText(this, "Sign Up functionality to be implemented", Toast.LENGTH_SHORT).show()
        }

        // Navigate to LoginActivity
        tvLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
