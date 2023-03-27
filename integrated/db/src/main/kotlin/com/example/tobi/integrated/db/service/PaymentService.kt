package com.example.tobi.integrated.db.service

import com.example.tobi.integrated.common.resultcode.ResultCode
import com.example.tobi.integrated.common.resultcode.ResultCodeException
import com.example.tobi.integrated.db.dto.payment.CreatePaymentDTO
import com.example.tobi.integrated.db.dto.payment.DeletePaymentDTO
import com.example.tobi.integrated.db.dto.payment.UpdatePaymentDTO
import com.example.tobi.integrated.db.entity.Package
import com.example.tobi.integrated.db.entity.Payment
import com.example.tobi.integrated.db.repository.PaymentRepository
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Service

@Service
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val pgService: PGService,
    private val packageService: PackageService,
) {
    companion object {
        private val log = LogManager.getLogger()
    }

    fun getPayment(id: Long?): Payment {
        log.debug("call getPayment : id = '$id'")

        if (id == null) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [id]이 존재하지 않습니다."
            )
        }

        try {
            return paymentRepository.findById(id)
                .orElseThrow {
                    ResultCodeException(
                        resultCode = ResultCode.ERROR_PAYMENT_NOT_EXISTS,
                        loglevel = Level.WARN,
                        message = "getPayment : id['${id}'] 컨텐츠 제공자가 존재하지 않습니다."
                    )
                }
        } catch(e: Exception) {
            log.error("getPayment DB search failed. ${id}", e)
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_DB,
                loglevel = Level.ERROR,
                message = "getPayment 호출 중 DB오류 발생 : ${e.message}"
            )
        }
    }

    fun createPayment(createPaymentDTO: CreatePaymentDTO): Payment {
        log.debug("call createPayment : createPaymentDTO = '$createPaymentDTO'")

        if (createPaymentDTO.pgId == null) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [pgId]이 존재하지 않습니다."
            )
        }

        if (createPaymentDTO.pkgId == null) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [pkgId]이 존재하지 않습니다."
            )
        }

        return try {
            paymentRepository.save(
                Payment(
                    pg = pgService.getPG(createPaymentDTO.pgId),
                    paidAt = createPaymentDTO.paidAt,
                    amount = createPaymentDTO.amount,
                    approveNo = createPaymentDTO.approveNo,
                    approveAt = createPaymentDTO.approveAt,
                    pkg = packageService.getPackage(createPaymentDTO.pkgId)
                )
            )
        } catch (e: Exception) {
            log.error("createPayment failed. $createPaymentDTO", e)
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_DB,
                loglevel = Level.ERROR,
                message = "createPayment 호출 중 DB오류 발생 : ${e.message}"
            )
        }
    }

    fun updatePayment(updatePaymentDTO: UpdatePaymentDTO): Payment {
        log.debug("call updatePayment : updatePaymentDTO = '$updatePaymentDTO'")

        if (updatePaymentDTO.id == null) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [ID]가 존재하지 않습니다."
            )
        }

        var isChange = false
        val payment = getPayment(updatePaymentDTO.id)

        if (updatePaymentDTO.pgId != null) {
            payment.pg = pgService.getPG(updatePaymentDTO.pgId)
            isChange = true
        }

        if (updatePaymentDTO.paidAt != null) {
            payment.paidAt = updatePaymentDTO.paidAt
            isChange = true
        }

        if (updatePaymentDTO.amount != null) {
            payment.amount = updatePaymentDTO.amount
            isChange = true
        }

        if (updatePaymentDTO.approveNo != null) {
            payment.approveNo = updatePaymentDTO.approveNo
            isChange = true
        }

        if (updatePaymentDTO.approveAt != null) {
            payment.approveAt = updatePaymentDTO.approveAt
            isChange = true
        }

        return try {
            when (isChange) {
                true -> paymentRepository.save(payment)
                else -> throw ResultCodeException(
                    resultCode = ResultCode.ERROR_NOTHING_TO_MODIFY,
                    loglevel = Level.INFO
                )
            }
        } catch (e: Exception) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_DB,
                loglevel = Level.ERROR,
                message = "updatePayment 호출 중 DB오류 발생 : ${e.message}"
            )
        }
    }

    fun deletePayment(deletePaymentDTO: DeletePaymentDTO) {
        log.debug("call deletePayment : deletePaymentDTO = '$deletePaymentDTO'")

        if (deletePaymentDTO.id == null) {
            throw ResultCodeException(
                ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [ID]가 존재하지 않습니다."
            )
        }

        if (isExistById(deletePaymentDTO.id)) {
            try {
                paymentRepository.deleteById(deletePaymentDTO.id)
            } catch (e: Exception) {
                throw ResultCodeException(
                    resultCode = ResultCode.ERROR_DB,
                    loglevel = Level.ERROR,
                    message = "deletePayment 호출 중 DB오류 발생 : ${e.message}"
                )
            }
        }
    }

    fun isExistById(id: Long): Boolean {
        log.debug("call isExistById : id = '$id'")

        return try {
            paymentRepository.existsById(id)
        } catch (e: Exception) {
            log.error("isExistByName DB search failed. $id", e)
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_DB,
                loglevel = Level.ERROR,
                message = "isExistByName 호출 중 DB오류 발생 : ${e.message}"
            )
        }
    }

    fun getPaymentsByPackage(thisPackage: Package): MutableList<Payment> {
        log.debug("call getPaymentsByPackage : thisPackage = '$thisPackage'")

        if (thisPackage == null) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [bundle]이 존재하지 않습니다."
            )
        }

        try {
            return paymentRepository.findByPkg(thisPackage)
        } catch(e: Exception) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_DB,
                loglevel = Level.ERROR,
                message = "getPaymentsByPackage 호출 중 DB오류 발생 : ${e.message}"
            )
        }
    }

    fun getPaymentsByApprove(approveNo: String): Payment {
        log.debug("call getPaymentsByApprove : approveNo = '$approveNo'")

        if (approveNo.isBlank()) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [approveNo]이 존재하지 않습니다."
            )
        }

        try {
            return paymentRepository.findByApproveNo(approveNo)
        } catch(e: Exception) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_DB,
                loglevel = Level.ERROR,
                message = "getPaymentsByApprove 호출 중 DB오류 발생 : ${e.message}"
            )
        }
    }
}