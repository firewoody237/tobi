package com.example.tobi.integrated.db.service.pg

interface PayService {

    fun payment(DTO)

    val mappingKeySet: Set<String>
}