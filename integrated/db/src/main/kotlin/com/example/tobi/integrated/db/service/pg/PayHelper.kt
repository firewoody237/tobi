package com.example.tobi.integrated.db.service.pg

import com.example.tobi.integrated.common.resultcode.ResultCode
import com.example.tobi.integrated.common.resultcode.ResultCodeException
import com.example.tobi.integrated.db.dto.bundle.PaymentDTO
import com.example.tobi.integrated.db.service.pg.dto.ResultDTO
import org.apache.logging.log4j.Level
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class PayHelper {
    private val payMapper: MutableMap<Long, PayService> = mutableMapOf()


    @Autowired
    private fun setPayHelper(list: List<PayService>) {
        list.forEach {
            it.mappingKeySet.forEach { key ->
                payMapper[key] = it
            }
        } //질문 : 이거 왜 mappingKeySet에 forEach하는지?
    }

    fun payment(paymentDTO: PaymentDTO): ResultDTO {
        val payService = getPayService(paymentDTO.pg!!.id) ?: throw ResultCodeException(
            resultCode = ResultCode.ERROR_PARAMETER_NOT_EXISTS,
            loglevel = Level.WARN,
            message = "파라미터에 [pgid]이 존재하지 않습니다."
        )
        return payService.payment(paymentDTO)
    }

    fun cancel(paymentDTO: PaymentDTO): ResultDTO {
        val payService = getPayService(paymentDTO.pg!!.id) ?: throw ResultCodeException(
            resultCode = ResultCode.ERROR_PARAMETER_NOT_EXISTS,
            loglevel = Level.WARN,
            message = "파라미터에 [pgid]이 존재하지 않습니다."
        )
        return payService.cancel(paymentDTO)
    }


    private fun getPayService(pgcd: Long): PayService? {
        return payMapper[pgcd]
    }


}