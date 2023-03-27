package com.example.tobi.integrated.db.service.pg.dto

data class ResultDTO(
    val pgId: Long,
    val approveNo: String,
    val approveDt: String,
    val approveTm: String,
    val approveAmount: Long,
)
