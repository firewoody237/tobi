package com.example.tobi.integrated.db.service

import com.example.tobi.integrated.common.resultcode.ResultCode
import com.example.tobi.integrated.common.resultcode.ResultCodeException
import com.example.tobi.integrated.db.dto.packages.CreatePackageDTO
import com.example.tobi.integrated.db.dto.packages.DeletePackageDTO
import com.example.tobi.integrated.db.dto.PayPackageDTO
import com.example.tobi.integrated.db.dto.packages.UpdatePackageDTO
import com.example.tobi.integrated.db.entity.Bundle
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Service
import com.example.tobi.integrated.db.entity.Package
import com.example.tobi.integrated.db.repository.PackageRepository
import org.apache.logging.log4j.Level
import java.time.LocalDateTime

@Service
class PackageService(
    private val packageRepository: PackageRepository,
    private val bundleService: BundleService,
    private val paymentService: PaymentService,
) {

    companion object {
        private val log = LogManager.getLogger()
    }

    fun getPackage(id: Long?): Package {
        log.debug("call getPackage : id = '$id'")

        if (id == null) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [id]이 존재하지 않습니다."
            )
        }

        try {
            return packageRepository.findById(id)
                .orElseThrow {
                    ResultCodeException(
                        resultCode = ResultCode.ERROR_PAYMENT_NOT_EXISTS,
                        loglevel = Level.WARN,
                        message = "getPackage : id['${id}'] 컨텐츠 제공자가 존재하지 않습니다."
                    )
                }
        } catch(e: Exception) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_DB,
                loglevel = Level.ERROR,
                message = "getPackage 호출 중 DB오류 발생 : ${e.message}"
            )
        }
    }

    fun createPackage(createPackageDTO: CreatePackageDTO): Package {
        log.debug("call createPackage : createPackageDTO = '$createPackageDTO'")

        if (createPackageDTO.itemId == null) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [itemId]이 존재하지 않습니다."
            )
        }

        if (createPackageDTO.amount == null) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [amount]이 존재하지 않습니다."
            )
        }

        if (createPackageDTO.bundleId == null) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [bundleId]이 존재하지 않습니다."
            )
        }

        if (createPackageDTO.quantity == null) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [quantity]이 존재하지 않습니다."
            )
        }

        return try {
            packageRepository.save(
                Package(
                    itemId = createPackageDTO.itemId,
                    paidAt = createPackageDTO.paidAt,
                    amount = createPackageDTO.amount,
                    paid = createPackageDTO.paid,
                    quantity = createPackageDTO.quantity,
                    bundle = bundleService.getBundle(createPackageDTO.bundleId)
                )
            )
        } catch (e: Exception) {
            log.error("createPackage failed. $createPackageDTO", e)
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_DB,
                loglevel = Level.ERROR,
                message = "createPackage 호출 중 DB오류 발생 : ${e.message}"
            )
        }
    }

    fun updatePackage(updatePackageDTO: UpdatePackageDTO): Package {
        log.debug("call updatePackage : updatePackageDTO = '$updatePackageDTO'")

        if (updatePackageDTO.id == null) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [ID]가 존재하지 않습니다."
            )
        }

        var isChange = false
        val thisPackage = getPackage(updatePackageDTO.id)

        if (updatePackageDTO.paidAt != null) {
            thisPackage.paidAt = updatePackageDTO.paidAt
            isChange = true
        }

        if (updatePackageDTO.amount != null) {
            thisPackage.amount = updatePackageDTO.amount
            isChange = true
        }

        if (updatePackageDTO.paid != null) {
            thisPackage.paid = updatePackageDTO.paid
            isChange = true
        }

        if (updatePackageDTO.quantity != null) {
            thisPackage.quantity = updatePackageDTO.quantity
            isChange = true
        }

        return try {
            when (isChange) {
                true -> packageRepository.save(thisPackage)
                else -> throw ResultCodeException(
                    resultCode = ResultCode.ERROR_NOTHING_TO_MODIFY,
                    loglevel = Level.INFO
                )
            }
        } catch (e: Exception) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_DB,
                loglevel = Level.ERROR,
                message = "updatePackage 호출 중 DB오류 발생 : ${e.message}"
            )
        }
    }

    fun deletePackage(deletePackageDTO: DeletePackageDTO) {
        log.debug("call deletePackage : deletePackageDTO = '$deletePackageDTO'")

        if (deletePackageDTO.id == null) {
            throw ResultCodeException(
                ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [ID]가 존재하지 않습니다."
            )
        }

        if (isExistById(deletePackageDTO.id)) {
            try {
                packageRepository.deleteById(deletePackageDTO.id)
            } catch (e: Exception) {
                throw ResultCodeException(
                    resultCode = ResultCode.ERROR_DB,
                    loglevel = Level.ERROR,
                    message = "deletePackage 호출 중 DB오류 발생 : ${e.message}"
                )
            }
        }
    }

    fun isExistById(id: Long): Boolean {
        log.debug("call isExistById : id = '$id'")

        return try {
            packageRepository.existsById(id)
        } catch (e: Exception) {
            log.error("isExistByName DB search failed. $id", e)
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_DB,
                loglevel = Level.ERROR,
                message = "isExistByName 호출 중 DB오류 발생 : ${e.message}"
            )
        }
    }

    fun payPackage(payPackageDTO: PayPackageDTO) {
        log.debug("call payPackage : payPackageDTO = '$payPackageDTO'")

        if (payPackageDTO.packageId == null) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [ID]가 존재하지 않습니다."
            )
        }
        val getPackage = getPackage(payPackageDTO.packageId)
        paymentService.createPayment(payPackageDTO.createPaymentDTO)
        updatePackage(
            UpdatePackageDTO(
                id = getPackage.id,
                paidAt = LocalDateTime.now(),
                quantity = getPackage.quantity,
                amount = getPackage.amount,
                paid = getPackage.paid?.plus(payPackageDTO.createPaymentDTO.amount!!)
            )
        )
    }

    fun getPackagesByBundle(bundle: Bundle?): MutableList<Package> {
        log.debug("call getPackagesByBundle : bundle = '$bundle'")

        if (bundle == null) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [bundle]이 존재하지 않습니다."
            )
        }

        try {
            return packageRepository.findByBundle(bundle)
        } catch(e: Exception) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_DB,
                loglevel = Level.ERROR,
                message = "getPackagesByBundle 호출 중 DB오류 발생 : ${e.message}"
            )
        }
    }
}