package com.example.tobi.integrated.db.dto.bundle

import com.example.tobi.integrated.db.service.pg.dto.PaymentDTO

data class AddPayToBundleDTO(
    val bundleId: Long?,
    val paymentDTO: PaymentDTO?,
    val amount: Long?,
)
