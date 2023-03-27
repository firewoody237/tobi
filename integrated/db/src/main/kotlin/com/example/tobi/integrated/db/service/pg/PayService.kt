package com.example.tobi.integrated.db.service.pg

import com.example.tobi.integrated.db.service.pg.dto.PaymentDTO
import com.example.tobi.integrated.db.service.pg.dto.ResultDTO

interface PayService {

    fun payment(dto: PaymentDTO): ResultDTO
    fun cancel(dto: PaymentDTO): ResultDTO

    val mappingKeySet: Set<Long>
}