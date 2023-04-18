package com.example.tobi.integrated.db.service.pg

import com.example.tobi.integrated.db.service.pg.dto.ResultDTO
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Service

@Service
class BankService() : PayService {
    companion object {
        private val log = LogManager.getLogger()
    }

    override fun payment(dto: PaymentDTO): ResultDTO {
        TODO("Not yet implemented")
        log.debug("BankService")
    }

    override fun cancel(dto: PaymentDTO): ResultDTO {
        TODO("Not yet implemented")
        log.debug("BankService - cancel")
    }

    override val mappingKeySet: Set<Long> = setOf(1L)
}