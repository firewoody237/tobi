package com.example.tobi.integrated.db.dto.packages

import java.time.LocalDateTime

data class CreatePackageDTO(
    val itemId: Long?,
    val paidAt: LocalDateTime?,
    val amount: Long?,
    val paid: Long? = 0,
    val quantity: Long?,
    val bundleId: Long?
)
