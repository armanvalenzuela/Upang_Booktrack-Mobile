package com.arc_templars.upangbooktrack

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface fetchNotifs {
    @GET("user_request_status.php")
    fun getNotifications(@Query("user_id") userId: Int): Call<NotificationResponse>
}

class UserNotifications : BottomSheetDialogFragment() {

    private lateinit var notificationRecyclerView: RecyclerView
    private lateinit var notificationAdapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_notifications, container, false)

        notificationRecyclerView = view.findViewById(R.id.rvNotifications)
        notificationRecyclerView.layoutManager = LinearLayoutManager(context)

        // GET USER ID FROM SHAREDPREF
        val sharedPreferences: SharedPreferences =
            requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("id", -1)  // Get user ID or default to -1 if not found

        if (userId != -1) {
            fetchNotifications(userId)
        } else {
            Toast.makeText(context, "User ID not found", Toast.LENGTH_SHORT).show()
        }

        // CLOSE BUTTON
        val closeButton: Button = view.findViewById(R.id.btnCloseNotifications)
        closeButton.setOnClickListener {
            dismiss()  // DISMISS THE SHEET ONCE CLOSED
        }

        return view
    }

    //FUNCT TO FETCH NOTIFICATION FOR THE SPECIFIC USER
    private fun fetchNotifications(userId: Int) {
        val apiService = ApiClient.getRetrofitInstance().create(fetchNotifs::class.java)

        apiService.getNotifications(userId).enqueue(object : Callback<NotificationResponse> {
            override fun onResponse(call: Call<NotificationResponse>, response: Response<NotificationResponse>) {
                if (response.isSuccessful) {
                    val notifications = response.body()?.notifications ?: emptyList()
                    notificationAdapter = NotificationAdapter(notifications)
                    notificationRecyclerView.adapter = notificationAdapter
                } else {
                    Toast.makeText(context, "Failed to fetch notifications", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<NotificationResponse>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}


