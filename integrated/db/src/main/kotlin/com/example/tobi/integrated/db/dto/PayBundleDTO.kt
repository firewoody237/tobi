package com.example.tobi.integrated.db.dto

data class PayBundleDTO(
    val bundleId: Long?,
    val payDTOList: MutableList<PayDTO>?
)
