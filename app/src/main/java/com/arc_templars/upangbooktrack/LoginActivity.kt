package com.arc_templars.upangbooktrack

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
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
        @Field("studentNo") studentNo: String,
        @Field("password") password: String
    ): Call<ResponseBody>
}

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etStudentNo = findViewById<EditText>(R.id.etStudentNo)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)

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
    }

    private fun sendLoginData(studentNo: String, password: String) {
        val loginApi = ApiClient.getRetrofitInstance().create(LoginApi::class.java)
        loginApi.login(studentNo, password).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val jsonResponse = response.body()?.string() ?: return
                    val jsonObject = JSONObject(jsonResponse)
                    val status = jsonObject.getString("status")

                    if (status == "success") {
                        val userId = jsonObject.getInt("id") // GETS ID SA DATABASE
                        val studentName = jsonObject.getString("studentName") // GETS STUDENT NAME

                        // SAVES DATA INTO SHAREDPREFERENCES
                        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putInt("id", userId)
                        editor.putString("studentName", studentName)
                        editor.putString("studentNo", studentNo)
                        editor.putString("password", password)
                        editor.apply()

                        Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()

                        // GOES TO START ACTIVITY KAPAG SUCCESSFUL
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                        finish() // CLOSE NA YUNG LOGIN ACTIVITY
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
