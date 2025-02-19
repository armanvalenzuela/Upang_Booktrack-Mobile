package com.arc_templars.upangbooktrack.models

data class Item(
    val name: String,
    val imageResId: Int, // Store drawable image resource ID
    val availability: Boolean // true = Available, false = Not Available
)
