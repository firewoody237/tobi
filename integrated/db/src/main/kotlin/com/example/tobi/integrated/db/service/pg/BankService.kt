package com.example.tobi.integrated.db.service.pg

import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Service

@Service
class BankService() : PayService {
    companion object {
        private val log = LogManager.getLogger()
    }

    override val mappingKeySet: Set<String> = setOf("1")
    override fun payment() {
        log.debug("BankService")
    }
}