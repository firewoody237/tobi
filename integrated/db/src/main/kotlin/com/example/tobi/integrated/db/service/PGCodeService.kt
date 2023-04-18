package com.example.tobi.integrated.db.service

import com.example.tobi.integrated.common.resultcode.ResultCode
import com.example.tobi.integrated.common.resultcode.ResultCodeException
import com.example.tobi.integrated.db.dto.pgcd.CreatePGCodeDTO
import com.example.tobi.integrated.db.dto.pgcd.DeletePGCodeDTO
import com.example.tobi.integrated.db.dto.pgcd.UpdatePGCodeDTO
import com.example.tobi.integrated.db.repository.PGCodeRepository
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Service

@Service
class PGCodeService(
    private val pgCodeRepository: PGCodeRepository
) {

    companion object {
        private val log = LogManager.getLogger()
    }

    fun getPGCode(id: Long?): PGCode {
        log.debug("call getPGCode : id = '$id'")

        if (id == null) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [id]이 존재하지 않습니다."
            )
        }

        try {
            return pgCodeRepository.findById(id)
                .orElseThrow {
                    ResultCodeException(
                        resultCode = ResultCode.ERROR_PGCODE_NOT_EXISTS,
                        loglevel = Level.WARN,
                        message = "getPGCode : id['${id}'] 컨텐츠 제공자가 존재하지 않습니다."
                    )
                }
        } catch(e: Exception) {
            log.error("getPGCode DB search failed. ${id}", e)
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_DB,
                loglevel = Level.ERROR,
                message = "getPGCode 호출 중 DB오류 발생 : ${e.message}"
            )
        }
    }

    fun createPGCode(createPGCodeDTO: CreatePGCodeDTO): PGCode {
        log.debug("call createPGCode : createPGCodeDTO = '$createPGCodeDTO'")

        if (createPGCodeDTO.name.isNullOrEmpty()) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [name]이 존재하지 않습니다."
            )
        }

        if (createPGCodeDTO.pgId == null) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [pgId]이 존재하지 않습니다."
            )
        }

        return when(isExistByName(createPGCodeDTO.name) || isExistByPgId(createPGCodeDTO.pgId)) {
            true -> {
                throw ResultCodeException(
                    resultCode = ResultCode.ERROR_PGCODE_ALREADY_EXISTS,
                    loglevel = Level.INFO,
                    message = "'${createPGCodeDTO.name}'은 이미 존재하는 PGCode입니다."
                )
            }
            false -> {
                try {
                    pgCodeRepository.save(
                        PGCode(
                            name = createPGCodeDTO.name,
                            pgId = createPGCodeDTO.pgId
                        )
                    )
                } catch (e: Exception) {
                    log.error("createPGCode failed. $createPGCodeDTO", e)
                    throw ResultCodeException(
                        resultCode = ResultCode.ERROR_DB,
                        loglevel = Level.ERROR,
                        message = "createPGCode 호출 중 DB오류 발생 : ${e.message}"
                    )
                }
            }
        }
    }

    fun updatePGCode(updatePGCodeDTO: UpdatePGCodeDTO): PGCode {
        log.debug("call updatePGCode : updatePGCodeDTO = '$updatePGCodeDTO'")

        if (updatePGCodeDTO.id == null) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [ID]가 존재하지 않습니다."
            )
        }

        if (updatePGCodeDTO.name?.isNotEmpty() == true) {
            if (isExistByName(updatePGCodeDTO.name)) {
                throw ResultCodeException(
                    resultCode = ResultCode.ERROR_USER_ALREADY_EXISTS,
                    loglevel = Level.INFO,
                    message = "'${updatePGCodeDTO.name}'은 이미 존재하는 [Name]입니다."
                )
            }
        }

        if (updatePGCodeDTO.pgId?.isNotEmpty() == true) {
            if (isExistByName(updatePGCodeDTO.pgId)) {
                throw ResultCodeException(
                    resultCode = ResultCode.ERROR_USER_ALREADY_EXISTS,
                    loglevel = Level.INFO,
                    message = "'${updatePGCodeDTO.name}'은 이미 존재하는 [pgId]입니다."
                )
            }
        }

        var isChange = false
        val pgCode = getPGCode(updatePGCodeDTO.id)

        if (updatePGCodeDTO.name?.isNotEmpty() == true) {
            pgCode.name = updatePGCodeDTO.name
            isChange = true
        }

        if (updatePGCodeDTO.pgId != null) {
            pgCode.pgId = updatePGCodeDTO.pgId
            isChange = true
        }

        return try {
            when (isChange) {
                true -> pgCodeRepository.save(pgCode)
                else -> throw ResultCodeException(
                    resultCode = ResultCode.ERROR_NOTHING_TO_MODIFY,
                    loglevel = Level.INFO
                )
            }
        } catch (e: Exception) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_DB,
                loglevel = Level.ERROR,
                message = "updatePGCode 호출 중 DB오류 발생 : ${e.message}"
            )
        }
    }

    fun deletePGCode(deletePGCodeDTO: DeletePGCodeDTO) {
        log.debug("call deletePGCode : deletePGCodeDTO = '$deletePGCodeDTO'")

        if (deletePGCodeDTO.id == null) {
            throw ResultCodeException(
                ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [ID]가 존재하지 않습니다."
            )
        }

        if (isExistById(deletePGCodeDTO.id)) {
            try {
                pgCodeRepository.deleteById(deletePGCodeDTO.id)
            } catch (e: Exception) {
                throw ResultCodeException(
                    resultCode = ResultCode.ERROR_DB,
                    loglevel = Level.ERROR,
                    message = "deletePGCode 호출 중 DB오류 발생 : ${e.message}"
                )
            }
        }

    }

    fun isExistByPgId(pgId: String): Boolean {
        log.debug("call isExistByPgId : pgId = '$pgId'")

        return try {
            pgCodeRepository.existsByPgId(pgId)
        } catch (e: Exception) {
            log.error("isExistByPgId DB search failed. $pgId", e)
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_DB,
                loglevel = Level.ERROR,
                message = "isExistByPgId 호출 중 DB오류 발생 : ${e.message}"
            )
        }
    }

    fun isExistByName(name: String): Boolean {
        log.debug("call isExistByName : name = '$name'")

        return try {
            pgCodeRepository.existsByName(name)
        } catch (e: Exception) {
            log.error("isExistByName DB search failed. $name", e)
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_DB,
                loglevel = Level.ERROR,
                message = "isExistByName 호출 중 DB오류 발생 : ${e.message}"
            )
        }
    }

    fun isExistById(id: Long): Boolean {
        log.debug("call isExistById : id = '$id'")

        return try {
            pgCodeRepository.existsById(id)
        } catch (e: Exception) {
            log.error("isExistById DB search failed. $id", e)
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_DB,
                loglevel = Level.ERROR,
                message = "isExistById 호출 중 DB오류 발생 : ${e.message}"
            )
        }
    }
}