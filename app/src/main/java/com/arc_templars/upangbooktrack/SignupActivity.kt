package com.arc_templars.upangbooktrack

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

// API Interface
interface SignupApi {
    @FormUrlEncoded
    @POST("signup.php")
    fun signup(
        @Field("first_name") firstName: String,
        @Field("last_name") lastName: String,
        @Field("gender") gender: String,
        @Field("college") college: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<ResponseBody>
}

class SignupActivity : AppCompatActivity() {

    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var radioGroupGender: RadioGroup
    private lateinit var spinnerDep: Spinner
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnSignUp: Button
    private lateinit var tvLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // INITIALIZE COMPONENTSSS
        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        radioGroupGender = findViewById(R.id.radioGroupGender)
        spinnerDep = findViewById(R.id.spinnerDep)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnSignUp = findViewById(R.id.btnSignUp)
        tvLogin = findViewById(R.id.tvLogin)

        //SPINNER NI ARMAN
        val departmentList = mutableListOf("Select a Department")
        departmentList.addAll(resources.getStringArray(R.array.departments))

        val departmentAdapter = object : ArrayAdapter<String>(
            this, android.R.layout.simple_spinner_item, departmentList
        ) {
            override fun isEnabled(position: Int): Boolean {
                return position != 0 // MAKES SURE NA MAY NAKA SELECT
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                (view as TextView).setTextColor(if (position == 0) Color.GRAY else Color.BLACK)
                return view
            }
        }
        departmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDep.adapter = departmentAdapter

        // SIGNUP BUTTON CLICK LISTENER
        btnSignUp.setOnClickListener {
            signUpUser()
        }

        // BALIK SA LOGIN
        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun signUpUser() {
        val firstName = etFirstName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()

        // Gender Selection
        val selectedGenderId = radioGroupGender.checkedRadioButtonId
        val gender = when (selectedGenderId) {
            R.id.rbMale -> "Male"
            R.id.rbFemale -> "Female"
            else -> ""
        }

        // COLLEGE SELECTION (KUNG ANO YUNG NAKA SELECT)
        val college = if (spinnerDep.selectedItemPosition > 0) spinnerDep.selectedItem.toString() else ""

        // VALIDATIONS HERE!
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() ||
            confirmPassword.isEmpty() || gender.isEmpty() || college.isEmpty()
        ) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters!", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show()
            return
        }

        // PROGRESS DIALOGUE BOX!!!
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Signing up...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        // API CALL
        val api = ApiClient.getRetrofitInstance().create(SignupApi::class.java)
        val call = api.signup(firstName, lastName, gender, college, email, password)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                progressDialog.dismiss()
                if (response.isSuccessful) {
                    Toast.makeText(applicationContext, "Registration successful! Check your email.", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(applicationContext, "Registration failed! Please try again.", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                progressDialog.dismiss()
                Toast.makeText(applicationContext, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
