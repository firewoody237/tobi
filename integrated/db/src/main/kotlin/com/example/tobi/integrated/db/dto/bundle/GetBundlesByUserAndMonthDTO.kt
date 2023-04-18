package com.example.tobi.integrated.db.dto.bundle

import java.time.LocalDateTime

data class GetBundlesByUserAndMonthDTO(
    val userId: Long?,
    val date: LocalDateTime?,
)
