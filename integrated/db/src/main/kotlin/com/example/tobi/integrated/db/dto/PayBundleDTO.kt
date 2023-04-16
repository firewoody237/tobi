package com.example.tobi.integrated.db.dto

data class PayBundleDTO(
    val bundleId: Long?,
    val surl : String?,
    val rparam : String?,
    val payDTOList: MutableList<PayDTO>?
)
