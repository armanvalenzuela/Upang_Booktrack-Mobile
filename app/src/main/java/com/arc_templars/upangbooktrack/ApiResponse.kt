package com.arc_templars.upangbooktrack

data class ApiResponse(
    val books: List<BookData>,
    val uniforms: List<UniformData>
)

data class BookData(
    val bookname: String,
    val bookimage: String,
    val bookstat: String,
    val bookcollege: String,
    val bookdesc: String,
    val bookstock: String
)

data class UniformData(
    val uniformname: String,
    val uniformimage: String,
    val uniformstat: String,
    val uniformcollege: String,
    val uniformdesc: String,
    val uniformsize: String,
    val uniformgender: String,
    val uniformstock: String
)