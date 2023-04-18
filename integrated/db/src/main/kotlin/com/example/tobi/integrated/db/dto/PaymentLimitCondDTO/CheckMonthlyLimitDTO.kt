package com.example.tobi.integrated.db.dto.PaymentLimitCondDTO

import com.example.tobi.integrated.db.dto.bundle.ItemDTO
import com.example.tobi.integrated.db.dto.bundle.PaymentDTO
import com.example.tobi.integrated.db.entity.PG

data class CheckMonthlyLimitDTO(
    val userId: Long?,
    val payList: MutableList<PaymentDTO>?,
)
