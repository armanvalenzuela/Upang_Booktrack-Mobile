package com.arc_templars.upangbooktrack

data class NotificationResponse(
    val success: Boolean,
    val notifications: List<Notification>
)

data class Notification(
    val notif_id: Int,
    val notifmessage: String,
    val notiftimestamp: String,
    val notifstatus: String,
    val itemName: String,
    val itemImage: String
)
