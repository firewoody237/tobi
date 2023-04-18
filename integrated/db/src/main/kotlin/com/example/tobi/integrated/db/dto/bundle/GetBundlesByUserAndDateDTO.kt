package com.example.tobi.integrated.db.dto.bundle

import java.time.LocalDateTime

data class GetBundlesByUserAndDateDTO(
    val userId: Long?,
    val date: LocalDateTime?,
)
