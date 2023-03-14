package com.example.tobi.integrated.db.model

data class User(
    val id: Long = 0L,
    var name: String? = "",
    var nickname: String? = "",
    var email: String? = "",
    var grade: String? = "GREEN",
    var point: Long = 0,
)
