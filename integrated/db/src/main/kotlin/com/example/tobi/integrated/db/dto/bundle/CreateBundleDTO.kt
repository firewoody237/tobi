package com.example.tobi.integrated.db.dto.bundle

import java.time.LocalDateTime

data class CreateBundleDTO(
    val userId: Long?,
    val amount: Long?,
    val itemList: MutableList<ItemDTO>?,
    val payList: MutableList<PaymentDTO>?,
    val surl: String?,
    val rparam: String?,
)
