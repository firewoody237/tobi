package com.example.tobi.integrated.db.dto

import java.time.LocalDateTime

data class UpdatePackageDTO(
    val id: Long?,
    val title: String?,
    val paidAt: LocalDateTime?,
    val quantity: Long?,
    val amount: Long?,
)
