package com.example.tobi.integrated.db.dto.bundle

import java.time.LocalDateTime

data class CancelBundleDTO(
    val originalBundleId: Long?,
    val surl: String?,
    val rparam: String?,
)
