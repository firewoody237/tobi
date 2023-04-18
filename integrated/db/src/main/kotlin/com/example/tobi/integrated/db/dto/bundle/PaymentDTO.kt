package com.example.tobi.integrated.db.dto.bundle

import com.example.tobi.integrated.db.entity.PG

data class PaymentDTO(
    val pg: PG?,
    var amount: Long?,
    val approveNo: String?,
)
