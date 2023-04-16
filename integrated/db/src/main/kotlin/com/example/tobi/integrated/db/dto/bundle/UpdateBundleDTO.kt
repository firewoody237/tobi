package com.example.tobi.integrated.db.dto.bundle

import java.time.LocalDateTime

data class UpdateBundleDTO(
    val id: Long?,
    val userId: Long?,
    val paidAt: LocalDateTime?,
    val amount: Long?,
    val cancelYn: Boolean?,
)
