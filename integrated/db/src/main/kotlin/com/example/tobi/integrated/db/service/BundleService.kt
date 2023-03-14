package com.example.tobi.integrated.db.service

import com.example.tobi.integrated.common.resultcode.ResultCode
import com.example.tobi.integrated.common.resultcode.ResultCodeException
import com.example.tobi.integrated.db.dto.CreateBundleDTO
import com.example.tobi.integrated.db.dto.DeleteBundleDTO
import com.example.tobi.integrated.db.dto.PayPackageDTO
import com.example.tobi.integrated.db.dto.UpdateBundleDTO
import com.example.tobi.integrated.db.entity.Bundle
import com.example.tobi.integrated.db.entity.Package
import com.example.tobi.integrated.db.model.Item
import com.example.tobi.integrated.db.repository.BundleRepository
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Service

@Service
class BundleService(
    private val bundleRepository: BundleRepository,
    private val packageService: PackageService,
    private val userApiService: UserApiService,
    private val itemApiService: ItemApiService,
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

    fun createBundle(createBundleDTO: CreateBundleDTO): Bundle {
        log.debug("call createBundle : createBundleDTO = '$createBundleDTO'")

        if (createBundleDTO.userId == null) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [userId]이 존재하지 않습니다."
            )
        }

        if (createBundleDTO.paidAt == null) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [paidAt]이 존재하지 않습니다."
            )
        }

        if (createBundleDTO.amount == null) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [amount]이 존재하지 않습니다."
            )
        }

        if (createBundleDTO.originalBundleId == null) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [originalBundleId]이 존재하지 않습니다."
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
            bundle?.userId = updateBundleDTO.userId
            isChange = true
        }

        if (updateBundleDTO.paidAt != null) {
            bundle?.paidAt = updateBundleDTO.paidAt
            isChange = true
        }

        if (updateBundleDTO.amount != null) {
            bundle?.amount = updateBundleDTO.amount
            isChange = true
        }

        if (updateBundleDTO.originalBundleId != null) {
            bundle?.originalBundleId = updateBundleDTO.originalBundleId
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


    }

    fun payBundle(bundleId: Long?): Boolean {
        //payPackage 호출
        log.debug("call payBundle : bundleId = '$bundleId'")

        if (bundleId == null) {
            throw ResultCodeException(
                ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [ID]가 존재하지 않습니다."
            )
        }

        val packageList = packageService.getPackagesByBundle(getBundle(bundleId))
            .forEach {
                packageService.payPackage(
                    PayPackageDTO(
                        packageId = it.id,
                        createPaymentDTO = null
                    )
                )
            }


        return null
    }

    fun cancelBundle(originalBundleId: Long?): Boolean {

    }

    fun addItemToBundle() {

    }

    fun removeItemFromBundle() {

    }
}