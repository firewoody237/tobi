package com.example.tobi.integrated.db.dto

import com.example.tobi.integrated.db.enum.PayMethod

data class PayDTO(
    val pgId: Long?,
    var amount: Long,
    val approveNo: String,
    val approveAt: String,
)
