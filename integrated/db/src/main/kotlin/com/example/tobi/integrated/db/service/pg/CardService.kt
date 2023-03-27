package com.example.tobi.integrated.db.service.pg

import com.example.tobi.integrated.db.service.pg.dto.PaymentDTO
import com.example.tobi.integrated.db.service.pg.dto.ResultDTO
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Service

@Service
class CardService() : PayService {
    companion object {
        private val log = LogManager.getLogger()
    }

    override fun payment(dto: PaymentDTO): ResultDTO {
        TODO("Not yet implemented")
        log.debug("CardService")
    }

    override fun cancel(dto: PaymentDTO): ResultDTO {
        TODO("Not yet implemented")
        log.debug("CardService - cancel")
    }

    override val mappingKeySet: Set<Long> = setOf(0L)
}