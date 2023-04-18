package com.example.tobi.integrated.db.service

import com.example.tobi.integrated.common.resultcode.ResultCode
import com.example.tobi.integrated.common.resultcode.ResultCodeException
import com.example.tobi.integrated.db.dto.*
import com.example.tobi.integrated.db.dto.bundle.*
import com.example.tobi.integrated.db.dto.packages.CreatePackageDTO
import com.example.tobi.integrated.db.dto.payment.CreatePaymentDTO
import com.example.tobi.integrated.db.entity.Bundle
import com.example.tobi.integrated.db.entity.Package
import com.example.tobi.integrated.db.repository.BundleRepository
import com.example.tobi.integrated.db.service.pg.PayHelper
import com.example.tobi.integrated.db.service.pg.dto.ResultDTO
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
        } catch (e: Exception) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_DB,
                loglevel = Level.ERROR,
                message = "getBundle 호출 중 DB오류 발생 : ${e.message}"
            )
        }
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

        return try {
            val bundle = bundleRepository.findByUserId(userId)
            if (bundle.isPresent) {
                bundle.get()
            } else {
                throw ResultCodeException(
                    resultCode = ResultCode.ERROR_BUNDLE_NOT_EXISTS,
                    loglevel = Level.WARN,
                )
            }
        } catch (e: Exception) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_DB,
                loglevel = Level.ERROR,
                message = "getBundleByUserId 호출 중 DB오류 발생 : ${e.message}"
            )
        }
    }

    @Transactional
    fun createBundle(createBundleDTO: CreateBundleDTO): String {
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

        if (createBundleDTO.itemList?.size!! < 1) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [itemList]이 존재하지 않습니다."
            )
        }

        if (createBundleDTO.payList?.size!! < 1) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [payList]이 존재하지 않습니다."
            )
        }

        val user = userApiService.getUserById(createBundleDTO.userId)

        val bundle = try {
            bundleRepository.save(
                Bundle(
                    userId = user.id,
                    amount = createBundleDTO.amount,
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

        createBundleDTO.itemList.forEach {  item ->
            addItemToBundle(
                AddItemToBundleDTO(
                    userId = user.id,
                    itemId = item.itemId,
                    quantity = item.qty,
                )
            )
        }

        if (bundle.amount != createBundleDTO.amount) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_DB,
                loglevel = Level.ERROR,
            )
        }

        val paidList: MutableList<ResultDTO> = mutableListOf()
        try {
            createBundleDTO.payList.forEach { pay ->
                val payResult = addPayToBundle(
                    AddPayToBundleDTO(
                        bundleId = bundle.id,
                        paymentDTO = pay,
                    )
                )
                paidList.add(payResult)
            }
        } catch (e: Exception) {
            paidList.forEach { pay ->
                abortPayments(pay)
            }
        }

        //call shopping surl(success url) -->
        return "${createBundleDTO.surl}?rparam=${createBundleDTO.rparam}"

        //1. 이 프로젝트의 경우 어디로 success콜백해줘야 하는걸까? 유저..?

        //fds, aml

        // talktalk, kakao talk, mail

        //recent pay method

        //receipt

        //kafka (mydata)
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

        if (updateBundleDTO.amount != null) {
            bundle.amount = updateBundleDTO.amount
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

    fun getPackagesByBundle(bundleId: Long?): MutableList<Package> {
        log.debug("call getPackagesByBundle : bundleId = '$bundleId'")

        if (bundleId == null) {
            throw ResultCodeException(
                ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [ID]가 존재하지 않습니다."
            )
        }

        return packageService.getPackagesByBundle(getBundle(bundleId))
    }

    fun abortPayments(payment: ResultDTO) {
        //payment에서 승인번호/승인시간 가지고 취소진행
        log.debug("call abortPayments : payment = '$payment'")

        if (payment.approveNo.isBlank()) {
            throw ResultCodeException(
                ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [approveNo]가 존재하지 않습니다."
            )
        }

        //pay찾아오기
        val pay = paymentService.getPaymentsByApprove(payment.approveNo)
        val pg = pgService.getPG(payment.pgId)

        val resultDto = payHelper.cancel(
            PaymentDTO(
                pg = pg,
                amount = pay.amount,
                approveNo = payment.approveNo,
            )
        )

        //TODO
    }

    @Transactional
    fun cancelBundle(cancelBundleDTO: CancelBundleDTO) {
        //로그
        //파라미터 검증
        log.debug("call cancelBundle : cancelBundleDTO = '$cancelBundleDTO'")

        if (cancelBundleDTO.originalBundleId == null) {
            throw ResultCodeException(
                ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [originalBundleId]가 존재하지 않습니다."
            )
        }

        //해당 번들이 결제가 되어있으면(+취소안되어있으면)
        val originalBundle = getBundle(cancelBundleDTO.originalBundleId)

        //-------------------

        val bundle = try {
            bundleRepository.save(
                Bundle(
                    userId = originalBundle.userId,
                    amount = originalBundle.amount * -1,
                )
            )
        } catch (e: Exception) {
            log.error("cancelBundle failed.")
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_DB,
                loglevel = Level.ERROR,
                message = "cancelBundle 호출 중 DB오류 발생 : ${e.message}"
            )
        }

        val paymentMap: MutableMap<Long, PaymentDTO> = mutableMapOf()
        getPackagesByBundle(cancelBundleDTO.originalBundleId).forEach { item ->
            addItemToBundle(
                AddItemToBundleDTO(
                    userId = originalBundle.userId,
                    itemId = item.id,
                    quantity = item.quantity?.times(-1)
                )
            )

            paymentService.getPaymentsByPackage(item).forEach { payment ->
                if (paymentMap.containsKey(payment.id)) {
                    paymentMap[payment.id]!!.amount = paymentMap[payment.id]!!.amount?.plus(payment.amount?.times(-1)!!)
                } else {
                    paymentMap[payment.id] = PaymentDTO(
                        pg = payment.pg,
                        amount = payment.amount?.times(-1),
                        approveNo = null,
                    )
                }
            }
        }

        val cancelList: MutableList<ResultDTO> = mutableListOf()
        paymentMap.forEach { payment ->
            val approve = payHelper.cancel(
                PaymentDTO(
                    pg = pgService.getPG(payment.key),
                    amount = payment.value.amount?.times(-1),
                    approveNo = payment.value.approveNo,
                )
            )
            cancelList.add(approve)
        }

        cancelList.forEach { cancel ->
            addPayToBundle(
                AddPayToBundleDTO(
                    bundleId = bundle.id,
                    paymentDTO = PaymentDTO(
                        pg = pgService.getPG(cancel.pgId),
                        amount = cancel.approveAmount,
                        approveNo = cancel.approveNo
                    )
                )
            )
        }
        //-------------------
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
                amount = bundle.amount + item.price?.times(addItemToBundleDTO.quantity)!!,
            )
        )
    }

    fun addPayToBundle(addPayToBundleDTO: AddPayToBundleDTO): ResultDTO {
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

//        if (addPayToBundleDTO.amount == null) {
//            throw ResultCodeException(
//                ResultCode.ERROR_PARAMETER_NOT_EXISTS,
//                loglevel = Level.WARN,
//                message = "파라미터에 [amount]가 존재하지 않습니다."
//            )
//        }


        val approve = payHelper.payment(
            addPayToBundleDTO.paymentDTO
        )

        val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        val dateTime = LocalDateTime.parse(approve.approveDt + approve.approveTm, formatter)

        val bundle = getBundle(addPayToBundleDTO.bundleId)
        val packagesOfBundle = packageService.getPackagesByBundle(bundle)

        var balance = approve.approveAmount

        packagesOfBundle.forEachIndexed { index, p ->
            val pg = pgService.getPG(addPayToBundleDTO.paymentDTO.pg!!.id)
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

        return approve
    }
}