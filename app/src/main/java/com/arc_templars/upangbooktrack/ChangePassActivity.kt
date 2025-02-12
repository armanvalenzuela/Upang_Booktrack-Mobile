package com.arc_templars.upangbooktrack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ChangePassApi {
    @FormUrlEncoded
    @POST("change_password.php")
    fun changePassword(
        @Field("id") userId: Int,
        @Field("newPassword") newPassword: String
    ): Call<ResponseBody>
}

class ChangePassActivity : AppCompatActivity() {

    private lateinit var etOldPass: EditText
    private lateinit var etNewPass: EditText
    private lateinit var etConfirmPass: EditText
    private lateinit var btnToggleOldPass: ImageView
    private lateinit var btnToggleNewPass: ImageView
    private lateinit var btnToggleConfirmPass: ImageView
    private lateinit var btnSubmit: ImageView

    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_pass)

        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getInt("id", -1)

        if (userId == -1) {
            Toast.makeText(this, "Error: User ID not found!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        etOldPass = findViewById(R.id.etOldPass)
        etNewPass = findViewById(R.id.etNewPass)
        etConfirmPass = findViewById(R.id.etConfirmPass)
        btnToggleOldPass = findViewById(R.id.btnToggleOldPass)
        btnToggleNewPass = findViewById(R.id.btnToggleNewPass)
        btnToggleConfirmPass = findViewById(R.id.btnToggleConfirmPass)
        btnSubmit = findViewById(R.id.btnSubmit)

        btnToggleNewPass.setOnClickListener { togglePasswordVisibility(etNewPass) }
        btnToggleConfirmPass.setOnClickListener { togglePasswordVisibility(etConfirmPass) }
        btnToggleOldPass.setOnClickListener { togglePasswordVisibility(etOldPass) }
        btnSubmit.setOnClickListener { validateAndChangePassword() }
    }

    private fun togglePasswordVisibility(editText: EditText) {
        if (editText.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        } else {
            editText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        }
        editText.setSelection(editText.text.length)
    }

    private fun validateAndChangePassword() {
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val storedOldPass = sharedPreferences.getString("password", "") ?: ""

        val oldPass = etOldPass.text.toString().trim()
        val newPass = etNewPass.text.toString().trim()
        val confirmPass = etConfirmPass.text.toString().trim()

        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (oldPass != storedOldPass) {
            Toast.makeText(this, "Old password is incorrect", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPass != confirmPass) {
            Toast.makeText(this, "New Password and Confirm Password must be the same", Toast.LENGTH_SHORT).show()
            return
        }

        changePassword(userId, newPass)
    }


    private fun changePassword(userId: Int, newPassword: String) {
        val api = ApiClient.getRetrofitInstance().create(ChangePassApi::class.java)

        api.changePassword(userId, newPassword).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ChangePassActivity, "Password changed successfully", Toast.LENGTH_SHORT).show()

                    // Clear SharedPreferences
                    val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                    sharedPreferences.edit().clear().apply()

                    // Redirect to LoginActivity
                    val intent = Intent(this@ChangePassActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@ChangePassActivity, "Failed to change password", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@ChangePassActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
