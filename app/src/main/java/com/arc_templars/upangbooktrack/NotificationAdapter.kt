package com.arc_templars.upangbooktrack

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface DeleteNotifService {
    @FormUrlEncoded
    @POST("user_update_notif.php")
    fun updateNotification(@Field("notif_id") notif_id: Int): Call<ResponseBody>
}

class NotificationAdapter(private val notifications: List<Notification>) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.notification_item, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.notificationTitle.text = notification.notifmessage
        holder.notificationTimestamp.text = notification.notiftimestamp
        holder.notificationStatus.text = notification.notifstatus

        // SET THE VALUES
        holder.notificationItemName.text = notification.itemName
        Glide.with(holder.itemView.context)
            .load(notification.itemImage)
            .into(holder.notifImage)

        //READ BUTTON CLICK LISTENER
        holder.haveReadButton.setOnClickListener {
            updateNotificationStatus(notification.notif_id, holder.itemView.context)
        }
    }

    override fun getItemCount(): Int = notifications.size

    // VIEWHOLDERS
    class NotificationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val notificationTitle: TextView = view.findViewById(R.id.notificationTitle)
        val notificationTimestamp: TextView = view.findViewById(R.id.notificationTimestamp)
        val notificationStatus: TextView = view.findViewById(R.id.notificationStatus)
        val notificationItemName: TextView = view.findViewById(R.id.notificationItemName)
        val notifImage: ImageView = view.findViewById(R.id.notifImage)
        val haveReadButton: ImageView= view.findViewById(R.id.haveReadbutton)
    }

    // DELETE NOTIFICATION FUNCT
    private fun updateNotificationStatus(notif_id: Int, context: Context) {
        Log.d("NotificationAdapter", "Updating notification status with ID: $notif_id")
        val apiService = ApiClient.getRetrofitInstance().create(DeleteNotifService::class.java)
        apiService.updateNotification(notif_id).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    // IF SUCCESSFUL
                    Toast.makeText(context, "Notification Read", Toast.LENGTH_SHORT).show()

                    // REMOVE THE NOTIF FROM THE LIST
                    val updatedNotifications = notifications.filter { it.notif_id != notif_id }
                    (notifications as MutableList).clear()
                    (notifications as MutableList).addAll(updatedNotifications)

                    // NOTIFY THE ADAPTER
                    notifyDataSetChanged()
                } else {
                    // IF FAILED
                    Toast.makeText(context, "Failed to update notification", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // IF FAILED
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
