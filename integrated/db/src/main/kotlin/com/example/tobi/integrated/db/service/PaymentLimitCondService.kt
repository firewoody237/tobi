package com.example.tobi.integrated.db.service

import com.example.tobi.integrated.common.resultcode.ResultCode
import com.example.tobi.integrated.common.resultcode.ResultCodeException
import com.example.tobi.integrated.db.dto.PaymentLimitCondDTO.CheckDailyLimitDTO
import com.example.tobi.integrated.db.dto.PaymentLimitCondDTO.CheckMonthlyLimitDTO
import com.example.tobi.integrated.db.dto.bundle.GetBundlesByUserAndDateDTO
import com.example.tobi.integrated.db.dto.bundle.GetBundlesByUserAndMonthDTO
import com.example.tobi.integrated.db.dto.bundle.PaymentDTO
import com.example.tobi.integrated.db.dto.payment.CreatePaymentDTO
import com.example.tobi.integrated.db.dto.payment.DeletePaymentDTO
import com.example.tobi.integrated.db.dto.payment.UpdatePaymentDTO
import com.example.tobi.integrated.db.entity.Package
import com.example.tobi.integrated.db.entity.Payment
import com.example.tobi.integrated.db.entity.PaymentLimitCond
import com.example.tobi.integrated.db.repository.PaymentLimitCondRepository
import com.example.tobi.integrated.db.repository.PaymentRepository
import com.sun.org.apache.xpath.internal.operations.Bool
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import kotlin.test.todo

@Service
class PaymentLimitCondService(
    private val paymentLimitCondRepository: PaymentLimitCondRepository,
    private val userApiService: UserApiService,
    private val bundleService: BundleService,
    private val packageService: PackageService,
    private val paymentService: PaymentService,
) {
    companion object {
        private val log = LogManager.getLogger()
    }

    fun checkTransactionLimit(payList: MutableList<PaymentDTO>) {
        log.debug("call checkTransactionLimit : payList = '$payList'")

        if (payList.isEmpty()) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [payList]이 존재하지 않습니다."
            )
        }

        payList.forEach { payment ->
            if (payment.pg == null) {
                throw ResultCodeException(
                    resultCode = ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                    loglevel = Level.WARN,
                    message = "payment [$payment]에 [pg]이 존재하지 않습니다."
                )
            }
            val paymentLimitCond = paymentLimitCondRepository.findByPg(payment.pg)
            if (paymentLimitCond.isPresent) {
                if (payment.amount!! > paymentLimitCond.get().transactionLimit!!) {
                    throw ResultCodeException(
                        resultCode = ResultCode.ERROR_PAY_LIMIT_COND_FAIL,
                        loglevel = Level.WARN,
                        message = "amt is ${payment.amount}. but transactionLimit is ${paymentLimitCond.get().transactionLimit!!}"
                    )
                }
            }
        }
    }

    fun checkDailyLimit(checkDailyLimitDTO: CheckDailyLimitDTO) {
        log.debug("call checkDailyLimit : checkDailyLimitDTO = '$checkDailyLimitDTO'")

        if (checkDailyLimitDTO.userId == null) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [userId]이 존재하지 않습니다."
            )
        }

        if (checkDailyLimitDTO.payList.isNullOrEmpty()) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [payList]이 존재하지 않습니다."
            )
        }

        val user = userApiService.getUserById(checkDailyLimitDTO.userId)

        checkDailyLimitDTO.payList.forEach { payment ->
            if (payment.pg == null) {
                throw ResultCodeException(
                    resultCode = ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                    loglevel = Level.WARN,
                    message = "payment [$payment]에 [pg]이 존재하지 않습니다."
                )
            }

            //유저별로 해당 pg sum 읽어오기
            //유저로 번들 찾고
            //그 번들로 패키지 찾고
            //그 패키지에서 페이먼트 찾고
            val bundleList = bundleService.getBundlesByUserIdAndDate(
                GetBundlesByUserAndDateDTO(
                    userId = user.id,
                    date = LocalDateTime.now()
                )
            )


            var sum = 0L
            bundleList.forEach { bundle ->
                packageService.getPackagesByBundle(bundle).forEach { myPackage ->
                    sum += paymentService.getPaymentsByPackage(myPackage).firstOrNull { it.pg == payment.pg }?.amount
                        ?: 0L
                }
            }

            //flatMap 알아보기

            val paymentLimitCond = paymentLimitCondRepository.findByPg(payment.pg)
            if (paymentLimitCond.isPresent) {
                if (sum > paymentLimitCond.get().dailyLimit!!) {
                    throw ResultCodeException(
                        resultCode = ResultCode.ERROR_PAY_LIMIT_COND_FAIL,
                        loglevel = Level.WARN,
                    )
                }
            }
        }
    }

    fun checkMonthlyLimit(checkMonthlyLimitDTO: CheckMonthlyLimitDTO) {
        log.debug("call checkMonthlyLimit : checkMonthlyLimitDTO = '$checkMonthlyLimitDTO'")

        if (checkMonthlyLimitDTO.userId == null) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [userId]이 존재하지 않습니다."
            )
        }

        if (checkMonthlyLimitDTO.payList.isNullOrEmpty()) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [payList]이 존재하지 않습니다."
            )
        }

        val user = userApiService.getUserById(checkMonthlyLimitDTO.userId)

        checkMonthlyLimitDTO.payList.forEach { payment ->

            if (payment.pg == null) {
                throw ResultCodeException(
                    resultCode = ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                    loglevel = Level.WARN,
                    message = "payment [$payment]에 [pg]이 존재하지 않습니다."
                )
            }

            //유저별로 해당 pg sum 읽어오기
            //유저로 번들 찾고
            //그 번들로 패키지 찾고
            //그 패키지에서 페이먼트 찾고
            val bundleList = bundleService.getBundlesByUserIdAndMonth(
                GetBundlesByUserAndMonthDTO(
                    userId = user.id,
                    date = LocalDateTime.now()
                )
            )

            var sum = 0L
            bundleList.forEach { bundle ->
                packageService.getPackagesByBundle(bundle).forEach { myPackage ->
                    sum += paymentService.getPaymentsByPackage(myPackage).filter { it.pg == payment.pg }[0].amount!!
                }
            }

            TODO("firstornull")

            val paymentLimitCond = paymentLimitCondRepository.findByPg(payment.pg)
            if (paymentLimitCond.isPresent) {
                if (sum > paymentLimitCond.get().monthlyLimit!!) {
                    throw ResultCodeException(
                        resultCode = ResultCode.ERROR_PAY_LIMIT_COND_FAIL,
                        loglevel = Level.WARN,
                    )
                }
            }
        }
    }
}