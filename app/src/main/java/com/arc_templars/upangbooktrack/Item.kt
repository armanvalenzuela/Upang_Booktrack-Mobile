package com.arc_templars.upangbooktrack.models

data class Item(
    val name: String,
    val imageResId: String,
    val availability: Boolean,
    val category: String,
    val department: String,
    val description: String,
    val size: String,
    val gender: String,
    val stock: Int
)
