package com.arc_templars.upangbooktrack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
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
    private lateinit var btnBackButton: ImageView
    private lateinit var btnToggleNewPass: ImageView
    private lateinit var btnToggleConfirmPass: ImageView
    private lateinit var btnSubmit: Button

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
        btnBackButton = findViewById(R.id.btnBackButton)
        btnSubmit = findViewById(R.id.btnSubmit)

        btnToggleOldPass.setOnClickListener { togglePasswordVisibility(etOldPass, btnToggleOldPass) }
        btnToggleNewPass.setOnClickListener { togglePasswordVisibility(etNewPass, btnToggleNewPass) }
        btnToggleConfirmPass.setOnClickListener { togglePasswordVisibility(etConfirmPass, btnToggleConfirmPass) }
        btnSubmit.setOnClickListener { validateAndChangePassword() }

        btnBackButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun togglePasswordVisibility(editText: EditText, toggleButton: ImageView) {
        if (editText.inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            editText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            toggleButton.setImageResource(R.drawable.eye_closed) //EYE ICON OPEN
        } else {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            toggleButton.setImageResource(R.drawable.eyelogo) // EYE ICON CLOSE
        }
        editText.setSelection(editText.text.length)
    }

    private fun validateAndChangePassword() {
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val storedOldPass = sharedPreferences.getString("password", "") ?: ""

        val oldPass = etOldPass.text.toString()
        val newPass = etNewPass.text.toString()
        val confirmPass = etConfirmPass.text.toString()

        // CHECK NEW PASS SPACES
        if (newPass.contains(" ")) {
            Toast.makeText(this, "Password cannot contain spaces", Toast.LENGTH_SHORT).show()
            return
        }

        // CHECK LENGTH
        val trimmedNewPass = newPass.trim()
        if (trimmedNewPass.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        // BALIDATIONS
        val trimmedOldPass = oldPass.trim()
        val trimmedConfirmPass = confirmPass.trim()

        if (trimmedOldPass.isEmpty() || trimmedNewPass.isEmpty() || trimmedConfirmPass.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (trimmedOldPass != storedOldPass) {
            Toast.makeText(this, "Old password is incorrect", Toast.LENGTH_SHORT).show()
            return
        }

        if (trimmedNewPass != trimmedConfirmPass) {
            Toast.makeText(this, "New passwords don't match", Toast.LENGTH_SHORT).show()
            return
        }

        if (trimmedNewPass == trimmedOldPass) {
            Toast.makeText(this, "New password must be different", Toast.LENGTH_SHORT).show()
            return
        }

        changePassword(userId, trimmedNewPass)
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
