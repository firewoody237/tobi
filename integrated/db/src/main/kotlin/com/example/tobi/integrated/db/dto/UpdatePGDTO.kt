package com.example.tobi.integrated.db.dto

import com.example.tobi.integrated.db.entity.PGCode

data class UpdatePGDTO(
    val id: Long?,
    val name: String?,
    val pgCodeId: Long?
)
