package com.arc_templars.upangbooktrack

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
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

interface ReadAllNotifsService {
    @FormUrlEncoded
    @POST("user_read_all_notifs.php")
    fun markAllAsRead(@Field("user_id") userId: Int): Call<ResponseBody>
}

class UserNotifications : BottomSheetDialogFragment() {

    private lateinit var notificationRecyclerView: RecyclerView
    private lateinit var notificationAdapter: NotificationAdapter

    override fun onStart() {
        super.onStart()
        dialog?.let {
            val bottomSheet = it.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT

            val behavior = BottomSheetBehavior.from(bottomSheet!!)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_notifications, container, false)

        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        notificationRecyclerView = view.findViewById(R.id.rvNotifications)
        notificationRecyclerView.layoutManager = LinearLayoutManager(context)

        val sharedPreferences: SharedPreferences =
            requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("id", -1)

        if (userId != -1) {
            fetchNotifications(userId)
        } else {
            Toast.makeText(context, "User ID not found", Toast.LENGTH_SHORT).show()
        }

        val closeButton: Button = view.findViewById(R.id.btnCloseNotifications)
        closeButton.setOnClickListener {
            dismiss()
        }

        val readAllButton: Button = view.findViewById(R.id.btnReadAllNotifications)
        readAllButton.setOnClickListener {
            if (::notificationAdapter.isInitialized && notificationAdapter.itemCount > 0) {
                markAllNotificationsAsRead(userId)
            } else {
                Toast.makeText(context, "No notifications!", Toast.LENGTH_SHORT).show()
                dismiss()
            }
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

                    if (notifications.isEmpty()) {
                        view?.findViewById<TextView>(R.id.tvNoNotifications)?.visibility = View.VISIBLE
                    } else {
                        view?.findViewById<TextView>(R.id.tvNoNotifications)?.visibility = View.GONE
                        notificationRecyclerView.visibility = View.VISIBLE
                        notificationAdapter = NotificationAdapter(notifications)
                        notificationRecyclerView.adapter = notificationAdapter
                    }
                } else {
                    Toast.makeText(context, "Failed to fetch notifications", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<NotificationResponse>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun markAllNotificationsAsRead(userId: Int) {
        val apiService = ApiClient.getRetrofitInstance().create(ReadAllNotifsService::class.java)

        apiService.markAllAsRead(userId).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "All notifications marked as read", Toast.LENGTH_SHORT).show()

                    // Refresh the list (clear notifications from RecyclerView)
                    notificationAdapter.notifyItemRangeRemoved(0, notificationAdapter.itemCount)
                    dismiss()
                } else {
                    Toast.makeText(context, "Failed to mark notifications as read", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


}


