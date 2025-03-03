package com.arc_templars.upangbooktrack.models

data class Item(
    val name: String,
    val imageResId: Int,
    val availability: Boolean,
    val category: String,
    val department: String,
    val stock: Int? = 0, // For books
    val sizes: String? = "" // For uniforms

)
