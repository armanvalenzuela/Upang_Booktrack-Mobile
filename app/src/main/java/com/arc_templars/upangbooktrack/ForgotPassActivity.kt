package com.arc_templars.upangbooktrack

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ForgotPassApi {
    @FormUrlEncoded
    @POST("forgot_password.php") // Ensure this matches your PHP script location
    fun forgotPassword(
        @Field("email") email: String // Sending email to the server
    ): Call<ResponseBody>
}

interface ResetPasswordApi {
    @FormUrlEncoded
    @POST("verify_reset_password.php") // Ensure this matches your PHP script
    fun resetPassword(
        @Field("email") email: String,
        @Field("otp") otp: String,
        @Field("newPassword") newPassword: String
    ): Call<ResponseBody>
}


class ForgotPassActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot_pass)

        val tvBacktoLogin = findViewById<TextView>(R.id.tvBacktoLogin)
        val btnSubmit = findViewById<ImageView>(R.id.btnSubmit)
        val btnReset = findViewById<Button>(R.id.btnReset)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etOTP = findViewById<EditText>(R.id.etOTP)
        val etNewPass = findViewById<EditText>(R.id.etNewPass)
        val etConfirmpass = findViewById<EditText>(R.id.etConfirmPass)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvBacktoLogin.setOnClickListener {
            val intent = Intent(this@ForgotPassActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnSubmit.setOnClickListener {
            val email = etEmail.text.toString()

            if (email.isNotEmpty()) {
                sendForgotPasswordData(email) // Call the function
            } else {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            }

        }

        btnReset.setOnClickListener {
            val email = etEmail.text.toString()
            val otp = etOTP.text.toString()
            val newPassword = etNewPass.text.toString()
            val confirmPassword = etConfirmpass.text.toString()

            if (email.isEmpty() || otp.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword.contains(" ")) {
                Toast.makeText(this, "Password cannot contain spaces", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sendResetPasswordData(email, otp, newPassword)
        }


    }

    private fun sendForgotPasswordData(email: String) {
        // Show a Toast message indicating that the email is being sent
        Toast.makeText(this, "Validating Email. Please Wait..", Toast.LENGTH_SHORT).show()

        val forgotPassApi = ApiClient.getRetrofitInstance().create(ForgotPassApi::class.java)

        forgotPassApi.forgotPassword(email).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: retrofit2.Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { responseBody ->
                        // Use the response string from the PHP backend
                        val responseMessage = responseBody.string()
                        Toast.makeText(this@ForgotPassActivity, responseMessage, Toast.LENGTH_SHORT)
                            .show()

                    } ?: run {
                        Toast.makeText(
                            this@ForgotPassActivity,
                            "Unexpected error occurred.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    response.errorBody()?.let { errorBody ->
                        Toast.makeText(
                            this@ForgotPassActivity,
                            errorBody.string(),
                            Toast.LENGTH_SHORT
                        ).show()
                    } ?: run {
                        Toast.makeText(
                            this@ForgotPassActivity,
                            "Failed to send email: ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@ForgotPassActivity, "Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })

    }

    private fun sendResetPasswordData(email: String, otp: String, newPassword: String) {
        Toast.makeText(this, "Resetting Password...", Toast.LENGTH_SHORT).show()

        val resetPassApi = ApiClient.getRetrofitInstance().create(ResetPasswordApi::class.java)

        resetPassApi.resetPassword(email, otp, newPassword).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: retrofit2.Response<ResponseBody>) {
                if (response.isSuccessful) {
                    response.body()?.let { responseBody ->
                        val responseMessage = responseBody.string()
                        Toast.makeText(this@ForgotPassActivity, responseMessage, Toast.LENGTH_SHORT).show()

                        if (responseMessage.contains("Password reset successful", true)) {
                            val intent = Intent(this@ForgotPassActivity, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        }

                    } ?: run {
                        Toast.makeText(this@ForgotPassActivity, "Unexpected error occurred.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    response.errorBody()?.let { errorBody ->
                        Toast.makeText(this@ForgotPassActivity, errorBody.string(), Toast.LENGTH_SHORT).show()
                    } ?: run {
                        Toast.makeText(this@ForgotPassActivity, "Failed to reset password: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@ForgotPassActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })

    }
}
