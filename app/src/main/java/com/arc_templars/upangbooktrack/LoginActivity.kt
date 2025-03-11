package com.arc_templars.upangbooktrack

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

// API Interface for Login
interface LoginApi {
    @FormUrlEncoded
    @POST("login.php") // Your actual PHP endpoint
    fun login(
        @Field("identifier") identifier: String,
        @Field("password") password: String
    ): Call<ResponseBody>
}

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etStudentNo = findViewById<EditText>(R.id.etStudentNo)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnTogglePass = findViewById<ImageView>(R.id.btnTogglePass)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)
        val tvSignUp = findViewById<TextView>(R.id.tvSignUp)

        btnLogin.setOnClickListener {
            val studentNo = etStudentNo.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (studentNo.isNotEmpty() && password.isNotEmpty()) {
                sendLoginData(studentNo, password)
            } else {
                Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show()
            }
        }

        tvForgotPassword.setOnClickListener{
            val intent = Intent (this@LoginActivity, ForgotPassActivity::class.java)
            startActivity(intent)
            finish()
        }

        tvSignUp.setOnClickListener{
            val intent = Intent (this@LoginActivity, SignupActivity::class.java)
            startActivity(intent)
            finish()
        }

        var isPasswordVisible = false

        btnTogglePass.setOnClickListener {
            if (isPasswordVisible) {

                // Hide password
                etPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                btnTogglePass.setImageResource(R.drawable.eyelogo) // Use an appropriate closed-eye icon
            } else {
                // Show password
                etPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                btnTogglePass.setImageResource(R.drawable.eye_closed) // Use an open-eye icon
            }
            etPassword.setSelection(etPassword.text.length) // Keep cursor at the end
            isPasswordVisible = !isPasswordVisible
        }

    }


    private fun sendLoginData(identifier: String, password: String) {
        val loginApi = ApiClient.getRetrofitInstance().create(LoginApi::class.java)
        loginApi.login(identifier, password).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val jsonResponse = response.body()?.string() ?: return
                    val jsonObject = JSONObject(jsonResponse)
                    val status = jsonObject.getString("status")

                    if (status == "success") {
                        val userId = jsonObject.getInt("id")
                        val studentName = jsonObject.getString("studentName")
                        val studentNo = jsonObject.getString("studentNo") // Get student number
                        val email = jsonObject.getString("email") // Get email

                        // Log for debugging
                        Log.d("LoginActivity", "User ID: $userId")
                        Log.d("LoginActivity", "Student Name: $studentName")
                        Log.d("LoginActivity", "Student No: $studentNo")
                        Log.d("LoginActivity", "Email: $email")

                        // Save user data into SharedPreferences
                        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                        with(sharedPreferences.edit()) {
                            putInt("id", userId)
                            putString("studentName", studentName)
                            putString("studentNo", studentNo) // Store student number
                            putString("email", email) // Store email
                            apply()
                        }

                        Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()

                        // Navigate to MainActivity
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Login failed: ${jsonObject.getString("message")}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
