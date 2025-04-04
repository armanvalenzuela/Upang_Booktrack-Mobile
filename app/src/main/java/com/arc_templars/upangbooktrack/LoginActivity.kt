package com.arc_templars.upangbooktrack

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
        etStudentNo.setHighlightColor(ContextCompat.getColor(this, R.color.green))
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnTogglePass = findViewById<ImageView>(R.id.btnTogglePass)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)
        val tvSignUp = findViewById<TextView>(R.id.tvSignUp)
        val rememberMeSwitch = findViewById<Switch>(R.id.rememberme)

        rememberMeSwitch.thumbTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green))
        rememberMeSwitch.trackTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.dark_green))

        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val isRemembered = sharedPreferences.getBoolean("rememberMe", false)

        if (isRemembered) {
            val savedStudentNo = sharedPreferences.getString("studentNo", "")
            val savedPassword = sharedPreferences.getString("password", "")
            val studentName = sharedPreferences.getString("studentName", "")

            if (!savedStudentNo.isNullOrEmpty() && !savedPassword.isNullOrEmpty()) {
                etStudentNo.setText(savedStudentNo)
                etPassword.setText(savedPassword)
                etPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                rememberMeSwitch.isChecked = true

                Toast.makeText(this, "Welcome back, $studentName!", Toast.LENGTH_SHORT).show()

                // Delay the transition slightly so the toast can be seen
                etStudentNo.postDelayed({
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                }, 1000) // 1-second delay
            }
        }

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
            startActivity(Intent(this@LoginActivity, ForgotPassActivity::class.java))
            finish()
        }

        tvSignUp.setOnClickListener{
            startActivity(Intent(this@LoginActivity, SignupActivity::class.java))
            finish()
        }

        var isPasswordVisible = false
        btnTogglePass.setOnClickListener {
            if (isPasswordVisible) {
                etPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                btnTogglePass.setImageResource(R.drawable.eyelogo)
            } else {
                etPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                btnTogglePass.setImageResource(R.drawable.eye_closed)
            }
            etPassword.setSelection(etPassword.text.length)
            isPasswordVisible = !isPasswordVisible
        }
    }

    private fun sendLoginData(identifier: String, password: String, isAutoLogin: Boolean = false) {
        val loginApi = ApiClient.getRetrofitInstance().create(LoginApi::class.java)
        loginApi.login(identifier, password).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    if (response.isSuccessful) {
                        val jsonResponse = response.body()?.string() ?: run {
                            Toast.makeText(this@LoginActivity, "Empty response", Toast.LENGTH_SHORT).show()
                            return
                        }

                        val jsonObject = JSONObject(jsonResponse)
                        when (jsonObject.getString("status")) {
                            "success" -> {
                                // Get all required data
                                val userId = jsonObject.getInt("id")
                                val studentName = jsonObject.getString("studentName")
                                val studentNo = jsonObject.getString("studentNo")
                                val email = jsonObject.getString("email")

                                // Save credentials using the correct SharedPreferences syntax
                                val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                                val editor = sharedPreferences.edit()
                                val rememberMeSwitch = findViewById<Switch>(R.id.rememberme)

                                editor.putInt("id", userId)
                                    .putString("studentName", studentName)
                                    .putString("studentNo", studentNo)
                                    .putString("email", email)

                                if (rememberMeSwitch.isChecked) {
                                    editor.putString("password", password)
                                        .putBoolean("rememberMe", true)
                                } else {
                                    editor.remove("password")
                                        .putBoolean("rememberMe", false)
                                }
                                editor.apply()

                                // Show welcome message
                                val welcomeMessage = if (isAutoLogin) {
                                    "Welcome back, $studentName!"
                                } else {
                                    "Log in successful!"
                                }
                                Toast.makeText(this@LoginActivity, welcomeMessage, Toast.LENGTH_SHORT).show()

                                // Navigate to MainActivity
                                val intent = Intent(this@LoginActivity, MainActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                }
                                startActivity(intent)
                                finish()
                            }
                            else -> {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Login failed: ${jsonObject.optString("message", "Unknown error")}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            "Error: ${response.errorBody()?.string() ?: response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Error processing response: ${e.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("LoginError", "Error parsing response", e)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(
                    this@LoginActivity,
                    "Network error: ${t.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("NetworkError", "Login failed", t)
            }
        })
    }
}
