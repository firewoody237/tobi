package com.example.tobi.integrated.db.model

data class Item(
    val id: Long = 0L,
    var name: String? = "",
    var price: Long? = 0L,
    var creatorId: Long? = 0L,
    var contentProvider: String? = "",
    var Category: String? = "",
)
