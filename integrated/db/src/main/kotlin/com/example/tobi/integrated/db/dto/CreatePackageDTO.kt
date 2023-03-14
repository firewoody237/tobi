package com.example.tobi.integrated.db.dto

import java.time.LocalDateTime

data class CreatePackageDTO(
    val title: String?,
    val itemId: Long?,
    val paidAt: LocalDateTime?,
    val amount: Long?,
    val bundleId: Long?
)
