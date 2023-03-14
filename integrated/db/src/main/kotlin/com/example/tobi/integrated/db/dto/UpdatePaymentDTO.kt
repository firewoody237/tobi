package com.example.tobi.integrated.db.dto

import java.time.LocalDateTime

data class UpdatePaymentDTO(
    val id: Long?,
    val pgId: Long?,
    val paidAt: LocalDateTime?,
    val amount: Long?,
    val approveNo: String?,
    val approveAt: String?,
)
