package com.example.tobi.integrated.db.dto.bundle

import java.time.LocalDateTime

data class CreateBundleDTO(
    val userId: Long?,
    val paidAt: LocalDateTime?,
    val amount: Long?,
    val originalBundleId: Long?
)
