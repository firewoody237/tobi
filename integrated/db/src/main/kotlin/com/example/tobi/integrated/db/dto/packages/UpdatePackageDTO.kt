package com.example.tobi.integrated.db.dto.packages

import java.time.LocalDateTime

data class UpdatePackageDTO(
    val id: Long?,
    val paidAt: LocalDateTime?,
    val quantity: Long?,
    val amount: Long?,
    val paid: Long?,
)
