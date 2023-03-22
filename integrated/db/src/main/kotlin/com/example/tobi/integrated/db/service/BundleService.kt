package com.example.tobi.integrated.db.service

import com.example.tobi.integrated.common.resultcode.ResultCode
import com.example.tobi.integrated.common.resultcode.ResultCodeException
import com.example.tobi.integrated.db.dto.*
import com.example.tobi.integrated.db.dto.bundle.*
import com.example.tobi.integrated.db.dto.packages.CreatePackageDTO
import com.example.tobi.integrated.db.dto.payment.CreatePaymentDTO
import com.example.tobi.integrated.db.entity.Bundle
import com.example.tobi.integrated.db.model.Item
import com.example.tobi.integrated.db.repository.BundleRepository
import com.example.tobi.integrated.db.service.pg.PayHelper
import com.example.tobi.integrated.db.service.pg.dto.PaymentDTO
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.transaction.Transactional

@Service
class BundleService(
    private val bundleRepository: BundleRepository,
    private val packageService: PackageService,
    private val paymentService: PaymentService,
    private val pgService: PGService,
    private val userApiService: UserApiService,
    private val itemApiService: ItemApiService,
    private val payHelper: PayHelper,
) {

    companion object {
        private val log = LogManager.getLogger()
    }

    fun getBundle(id: Long?): Bundle {
        log.debug("call getBundle : id = '$id'")

        if (id == null) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [id]이 존재하지 않습니다."
            )
        }

        try {
            return bundleRepository.findById(id)
                .orElseThrow {
                    ResultCodeException(
                        resultCode = ResultCode.ERROR_BUNDLE_NOT_EXISTS,
                        loglevel = Level.WARN,
                        message = "getBundle : id['${id}'] 번들이 존재하지 않습니다."
                    )
                }
        } catch(e: Exception) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_DB,
                loglevel = Level.ERROR,
                message = "getBundle 호출 중 DB오류 발생 : ${e.message}"
            )
        }
    }

    fun a() {
        val a = listOf(1, 2, 3).sortedDescending()
    }

    fun getBundleByUserId(userId: Long?): Bundle {
        log.debug("call getBundleByUserId : userId = '$userId'")

        if (userId == null) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [userId]이 존재하지 않습니다."
            )
        }

        try {
            val optionalBundle = bundleRepository.findByUserIdAndPaidAtIsNull(userId)
            if (optionalBundle.isPresent) {
                return optionalBundle.get()
            } else {
                return createBundle(
                    CreateBundleDTO(
                        userId = userId,
                        paidAt = null,
                        amount = 0L,
                        originalBundleId = null
                    )
                )
            }
        } catch(e: Exception) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_DB,
                loglevel = Level.ERROR,
                message = "getBundleByUserId 호출 중 DB오류 발생 : ${e.message}"
            )
        }
    }

    fun createBundle(createBundleDTO: CreateBundleDTO): Bundle {
        log.debug("call createBundle : createBundleDTO = '$createBundleDTO'")

        if (createBundleDTO.userId == null) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [userId]이 존재하지 않습니다."
            )
        }

        if (createBundleDTO.amount == null) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [amount]이 존재하지 않습니다."
            )
        }

        val user = userApiService.getUserById(createBundleDTO.userId)

        return try {
            bundleRepository.save(
                Bundle(
                    userId = user.id,
                    paidAt = createBundleDTO.paidAt,
                    amount = createBundleDTO.amount,
                    originalBundleId = createBundleDTO.originalBundleId
                )
            )
        } catch (e: Exception) {
            log.error("createBundle failed. $createBundleDTO", e)
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_DB,
                loglevel = Level.ERROR,
                message = "createBundle 호출 중 DB오류 발생 : ${e.message}"
            )
        }
    }

    fun updateBundle(updateBundleDTO: UpdateBundleDTO): Bundle {
        log.debug("call updateBundle : updateBundleDTO = '$updateBundleDTO'")

        if (updateBundleDTO.id == null) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [ID]가 존재하지 않습니다."
            )
        }

        var isChange = false
        val bundle = getBundle(updateBundleDTO.id)

        if (updateBundleDTO.userId != null) {
            bundle.userId = updateBundleDTO.userId
            isChange = true
        }

        if (updateBundleDTO.paidAt != null) {
            bundle.paidAt = updateBundleDTO.paidAt
            isChange = true
        }

        if (updateBundleDTO.amount != null) {
            bundle.amount = updateBundleDTO.amount
            isChange = true
        }

        if (updateBundleDTO.originalBundleId != null) {
            bundle.originalBundleId = updateBundleDTO.originalBundleId
            isChange = true
        }

        return try {
            when (isChange) {
                true -> bundleRepository.save(bundle)
                else -> throw ResultCodeException(
                    resultCode = ResultCode.ERROR_NOTHING_TO_MODIFY,
                    loglevel = Level.INFO
                )
            }
        } catch (e: Exception) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_DB,
                loglevel = Level.ERROR,
                message = "updateBundle 호출 중 DB오류 발생 : ${e.message}"
            )
        }
    }

    fun deleteBundle(deleteBundleDTO: DeleteBundleDTO) {
        log.debug("call deleteBundle : deleteBundleDTO = '$deleteBundleDTO'")

        if (deleteBundleDTO.id == null) {
            throw ResultCodeException(
                ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [ID]가 존재하지 않습니다."
            )
        }

        if (isExistById(deleteBundleDTO.id)) {
            try {
                bundleRepository.deleteById(deleteBundleDTO.id)
            } catch (e: Exception) {
                throw ResultCodeException(
                    resultCode = ResultCode.ERROR_DB,
                    loglevel = Level.ERROR,
                    message = "deleteBundle 호출 중 DB오류 발생 : ${e.message}"
                )
            }
        }
    }

    fun isExistById(id: Long): Boolean {
        log.debug("call isExistById : id = '$id'")

        return try {
            bundleRepository.existsById(id)
        } catch (e: Exception) {
            log.error("isExistByName DB search failed. $id", e)
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_DB,
                loglevel = Level.ERROR,
                message = "isExistByName 호출 중 DB오류 발생 : ${e.message}"
            )
        }
    }

    fun getItemsByBundle(bundleId: Long?): MutableList<Item> {
        log.debug("call getItemsByBundle : bundleId = '$bundleId'")

        return mutableListOf()
    }

    fun payBundle(payBundleDTO: PayBundleDTO) {
        //payPackage 호출
        log.debug("call payBundle : payBundleDTO = '$payBundleDTO'")

        if (payBundleDTO.bundleId == null) {
            throw ResultCodeException(
                ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [bundleId]가 존재하지 않습니다."
            )
        }

        if (payBundleDTO.payDTOList == null) {
            throw ResultCodeException(
                ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [payDTOList]가 존재하지 않습니다."
            )
        }

        val bundle = getBundle(payBundleDTO.bundleId)
        if (payBundleDTO.payDTOList.sumOf { it.amount } != bundle.amount) {
            throw ResultCodeException(
                ResultCode.ERROR_BUNDLE_AMOUNT_NOT_MATCHED,
                loglevel = Level.WARN,
            )
        }

        payBundleDTO.payDTOList.forEach {
            addPayToBundle(
                AddPayToBundleDTO(
                    bundleId = bundle.id,
                    paymentDTO = PaymentDTO(pgService.getPG(it.pgId).id),
                    amount = it.amount
                )
            )
        }
    }

    fun cancelBundle(originalBundleId: Long?) {
        //해당 번들이 결제가 되어있으면
        //각 페이먼트 서비스에 취소 호출
    }

    @Transactional
    fun addItemToBundle(addItemToBundleDTO: AddItemToBundleDTO) {
        log.debug("call addItemToBundle : addItemToBundleDTO = '$addItemToBundleDTO'")

        if (addItemToBundleDTO.userId == null) {
            throw ResultCodeException(
                ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [userId]가 존재하지 않습니다."
            )
        }

        if (addItemToBundleDTO.itemId == null) {
            throw ResultCodeException(
                ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [itemId]가 존재하지 않습니다."
            )
        }

        if (addItemToBundleDTO.quantity == null) {
            throw ResultCodeException(
                ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [quantity]가 존재하지 않습니다."
            )
        }

        val item = itemApiService.getItemById(itemId = addItemToBundleDTO.itemId)
        val user = userApiService.getUserById(userId = addItemToBundleDTO.userId)
        val bundle = getBundleByUserId(user.id)

        packageService.createPackage(
            CreatePackageDTO(
                itemId = item.id,
                paidAt = null,
                amount = item.price?.times(addItemToBundleDTO.quantity),
                quantity = addItemToBundleDTO.quantity,
                bundleId = bundle.id
            )
        )

        updateBundle(
            UpdateBundleDTO(
                id = bundle.id,
                userId = user.id,
                paidAt = bundle.paidAt,
                amount = bundle.amount + item.price?.times(addItemToBundleDTO.quantity)!!,
                originalBundleId = bundle.originalBundleId
            )
        )
    }

    fun removeItemFromBundle() {
        //번들 확인하고 결제 없으면
        //해당 아이템 제거
    }

    fun addPayToBundle(addPayToBundleDTO: AddPayToBundleDTO) {
        log.debug("call addPayToBundle : addPayToBundleDTO = '$addPayToBundleDTO'")

        if (addPayToBundleDTO.bundleId == null) {
            throw ResultCodeException(
                ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [bundleId]가 존재하지 않습니다."
            )
        }

        if (addPayToBundleDTO.paymentDTO == null) {
            throw ResultCodeException(
                ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [payMethod]가 존재하지 않습니다."
            )
        }

        if (addPayToBundleDTO.amount == null) {
            throw ResultCodeException(
                ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [amount]가 존재하지 않습니다."
            )
        }

        val approve = PayHelper().payment(
            addPayToBundleDTO.paymentDTO
        )

        val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        val dateTime = LocalDateTime.parse(approve.approveDt + approve.approveTm, formatter)

        val bundle = getBundle(addPayToBundleDTO.bundleId)
        val packagesOfBundle = packageService.getPackagesByBundle(bundle)

        var balance = approve.approveAmount

        packagesOfBundle.forEachIndexed { index, p ->
            val pg = pgService.getPG(addPayToBundleDTO.paymentDTO.pgId)
            if (index == packagesOfBundle.size - 1) {
                packageService.payPackage(
                    PayPackageDTO(
                        packageId = p.id,
                        createPaymentDTO = CreatePaymentDTO(
                            pgId = pg.id,
                            paidAt = dateTime,
                            amount = balance,
                            approveNo = approve.approveNo,
                            approveAt = approve.approveDt + approve.approveTm,
                            pkgId = p.id
                        )
                    )
                )
            } else {
                val rate = p.amount?.toDouble()?.div(bundle.amount.toDouble())
                val choppedAmount = approve.approveAmount * rate!!
                paymentService.createPayment(
                    CreatePaymentDTO(
                        pgId = pg.id,
                        paidAt = dateTime,
                        amount = choppedAmount.toLong(),
                        approveNo = approve.approveNo,
                        approveAt = approve.approveDt + approve.approveTm,
                        pkgId = p.id
                    )
                )
                balance -= choppedAmount.toLong()
            }
        }
    }
}