package com.example.tobi.integrated.db.dto

import com.example.tobi.integrated.db.dto.payment.CreatePaymentDTO

data class PayPackageDTO(
    val packageId: Long?,
    val createPaymentDTO: CreatePaymentDTO,
)
