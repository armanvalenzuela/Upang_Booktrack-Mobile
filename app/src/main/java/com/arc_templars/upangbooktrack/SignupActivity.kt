package com.arc_templars.upangbooktrack

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import okhttp3.ResponseBody
import org.json.JSONObject
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
    private lateinit var tvSelectedDepartment: TextView
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var ivTogglePassword: ImageView
    private lateinit var etConfirmPassword: EditText
    private lateinit var ivToggleConfirmPassword: ImageView
    private lateinit var btnSignUp: Button
    private lateinit var tvLogin: TextView

    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // INITIALIZE COMPONENTSSS
        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        radioGroupGender = findViewById(R.id.radioGroupGender)
        tvSelectedDepartment = findViewById(R.id.tvSelectedDepartment)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        ivTogglePassword = findViewById(R.id.ivTogglePassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        ivToggleConfirmPassword = findViewById(R.id.ivToggleConfirmPassword)
        btnSignUp = findViewById(R.id.btnSignUp)
        tvLogin = findViewById(R.id.tvLogin)

        // Click Listener for Department Selection
        tvSelectedDepartment.setOnClickListener {
            showDepartmentSelectionDialog()
        }

        // PASSWORD TOGGLE FUNCTIONALITY
        ivTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            togglePasswordVisibility(etPassword, ivTogglePassword, isPasswordVisible)
        }

        ivToggleConfirmPassword.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            togglePasswordVisibility(etConfirmPassword, ivToggleConfirmPassword, isConfirmPasswordVisible)
        }

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

    // Function to Show Custom AlertDialog for Department Selection
    private fun showDepartmentSelectionDialog() {
        val departmentList = resources.getStringArray(R.array.departments)

        // Inflate the custom layout
        val dialogView = layoutInflater.inflate(R.layout.dialog_department_selection, null)

        // Find UI components in the custom layout
        val listView = dialogView.findViewById<ListView>(R.id.listViewDepartments)

        // Create & set adapter for ListView
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, departmentList)
        listView.adapter = adapter

        // Create AlertDialog
        val dialog = AlertDialog.Builder(this, R.style.CustomDialog) // ✅ Use Custom Style
            .setView(dialogView)
            .create()

        // Handle item selection
        listView.setOnItemClickListener { _, _, position, _ ->
            tvSelectedDepartment.text = departmentList[position] // ✅ Updates TextView
            dialog.dismiss()
        }

        // Show dialog
        dialog.show()

        // Set custom width & height
        val window = dialog.window
        window?.setLayout(500, 1000) // ✅ Adjust width & height (in pixels)
        window?.setBackgroundDrawableResource(R.drawable.dialog_background) // ✅ Apply rounded corners
    }


    private fun togglePasswordVisibility(editText: EditText, imageView: ImageView, isVisible: Boolean) {
        if (isVisible) {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            imageView.setImageResource(R.drawable.eye_closed) // Open eye icon
        } else {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            imageView.setImageResource(R.drawable.eyelogo) // Closed eye icon
        }
        editText.setSelection(editText.text.length) // Keep cursor at end
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

        // College Selection
        val college = if (tvSelectedDepartment.text != "Select Department") tvSelectedDepartment.text.toString() else ""

        // VALIDATION CHECKS
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() ||
            confirmPassword.isEmpty() || gender.isEmpty() || college.isEmpty()
        ) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show()
            return
        }

        //EMPTY SPACE CHECKS
        if (firstName.contains(" ")) {
            Toast.makeText(this, "First name cannot contain spaces!", Toast.LENGTH_SHORT).show()
            return
        }
        if (lastName.contains(" ")) {
            Toast.makeText(this, "Last name cannot contain spaces!", Toast.LENGTH_SHORT).show()
            return
        }
        if (email.contains(" ")) {
            Toast.makeText(this, "Email cannot contain spaces!", Toast.LENGTH_SHORT).show()
            return
        }
        if (password.contains(" ")) {
            Toast.makeText(this, "Password cannot contain spaces!", Toast.LENGTH_SHORT).show()
            return
        }
        if (confirmPassword.contains(" ")) {
            Toast.makeText(this, "Confirm password cannot contain spaces!", Toast.LENGTH_SHORT).show()
            return
        }
        if (gender.contains(" ")) {
            Toast.makeText(this, "Gender cannot contain spaces!", Toast.LENGTH_SHORT).show()
            return
        }
        if (college.contains(" ")) {
            Toast.makeText(this, "College cannot contain spaces!", Toast.LENGTH_SHORT).show()
            return
        }

        //CLIENT SIDE EMAIL VALIDATION (PHP ALSO DOES IT)
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email!", Toast.LENGTH_SHORT).show()
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

        //Proceed with signup process if all checks pass
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Signing up...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val api = ApiClient.getRetrofitInstance().create(SignupApi::class.java)
        val call = api.signup(firstName, lastName, gender, college, email, password)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                progressDialog.dismiss()
                if (response.isSuccessful) {
                    val jsonResponse = response.body()?.string() ?: "{}"
                    val jsonObject = JSONObject(jsonResponse)

                    val status = jsonObject.optString("status", "error")
                    val message = jsonObject.optString("message", "Something went wrong.")

                    if (status == "success") {
                        Toast.makeText(applicationContext, "Registration successful!", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(applicationContext, "Registration failed: $message", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(applicationContext, "Server error: ${response.message()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                progressDialog.dismiss()
                Toast.makeText(applicationContext, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
