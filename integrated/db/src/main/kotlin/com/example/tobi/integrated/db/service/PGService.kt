package com.example.tobi.integrated.db.service

import com.example.tobi.integrated.common.resultcode.ResultCode
import com.example.tobi.integrated.common.resultcode.ResultCodeException
import com.example.tobi.integrated.db.dto.pg.CreatePGDTO
import com.example.tobi.integrated.db.dto.pg.DeletePGDTO
import com.example.tobi.integrated.db.dto.pg.UpdatePGDTO
import com.example.tobi.integrated.db.entity.PG
import com.example.tobi.integrated.db.repository.PGRepository
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Service

@Service
class PGService(
    private val pgRepository: PGRepository,
    private val pgCodeService: PGCodeService,
) {

    companion object {
        private val log = LogManager.getLogger()
    }

    fun getPG(id: Long?): PG {
        log.debug("call getPGService : id = '$id'")

        if (id == null) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [id]이 존재하지 않습니다."
            )
        }

        try {
            return pgRepository.findById(id)
                .orElseThrow {
                    ResultCodeException(
                        resultCode = ResultCode.ERROR_PG_NOT_EXISTS,
                        loglevel = Level.WARN,
                        message = "getPG : id['${id}'] 컨텐츠 제공자가 존재하지 않습니다."
                    )
                }
        } catch(e: Exception) {
            log.error("getPG DB search failed. ${id}", e)
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_DB,
                loglevel = Level.ERROR,
                message = "getPG 호출 중 DB오류 발생 : ${e.message}"
            )
        }
    }

    fun createPG(createPGDTO: CreatePGDTO): PG {
        log.debug("call createPG : createPGDTO = '$createPGDTO'")

        if (createPGDTO.name.isNullOrEmpty()) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [name]이 존재하지 않습니다."
            )
        }

        if (createPGDTO.pgCodeId == null) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [pgCode]이 존재하지 않습니다."
            )
        }

        val pgCode = pgCodeService.getPGCode(createPGDTO.pgCodeId)

        return when(isExistByName(createPGDTO.name)) {
            true -> {
                throw ResultCodeException(
                    resultCode = ResultCode.ERROR_PG_ALREADY_EXISTS,
                    loglevel = Level.INFO,
                    message = "'${createPGDTO.name}'은 이미 존재하는 [Name]입니다."
                )
            }
            false -> {
                try {
                    pgRepository.save(
                        PG(
                            name = createPGDTO.name,
                            pgCode = pgCode
                        )
                    )
                } catch (e: Exception) {
                    log.error("createPG failed. $createPGDTO", e)
                    throw ResultCodeException(
                        resultCode = ResultCode.ERROR_DB,
                        loglevel = Level.ERROR,
                        message = "createPG 호출 중 DB오류 발생 : ${e.message}"
                    )
                }
            }
        }
    }

    fun updatePG(updatePGDTO: UpdatePGDTO): PG {
        log.debug("call updatePG : updatePGDTO = '$updatePGDTO'")

        if (updatePGDTO.id == null) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [ID]가 존재하지 않습니다."
            )
        }

        if (updatePGDTO.name?.isNotEmpty() == true) {
            if (isExistByName(updatePGDTO.name)) {
                throw ResultCodeException(
                    resultCode = ResultCode.ERROR_USER_ALREADY_EXISTS,
                    loglevel = Level.INFO,
                    message = "'${updatePGDTO.name}'은 이미 존재하는 [Name]입니다."
                )
            }
        }

        var isChange = false
        val pg = getPG(updatePGDTO.id)

        if (updatePGDTO.name?.isNotEmpty() == true) {
            pg.name = updatePGDTO.name
            isChange = true
        }

        if (updatePGDTO.pgCodeId != null) {
            pg.pgCode = pgCodeService.getPGCode(updatePGDTO.pgCodeId)
            isChange = true
        }

        return try {
            when (isChange) {
                true -> pgRepository.save(pg)
                else -> throw ResultCodeException(
                    resultCode = ResultCode.ERROR_NOTHING_TO_MODIFY,
                    loglevel = Level.INFO
                )
            }
        } catch (e: Exception) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_DB,
                loglevel = Level.ERROR,
                message = "updatePG 호출 중 DB오류 발생 : ${e.message}"
            )
        }
    }

    fun deletePG(deletePGDTO: DeletePGDTO) {
        log.debug("call deletePG : deletePGDTO = '$deletePGDTO'")

        if (deletePGDTO.id == null) {
            throw ResultCodeException(
                ResultCode.ERROR_PARAMETER_NOT_EXISTS,
                loglevel = Level.WARN,
                message = "파라미터에 [ID]가 존재하지 않습니다."
            )
        }

        if (isExistById(deletePGDTO.id)) {
            try {
                pgRepository.deleteById(deletePGDTO.id)
            } catch (e: Exception) {
                throw ResultCodeException(
                    resultCode = ResultCode.ERROR_DB,
                    loglevel = Level.ERROR,
                    message = "deletePG 호출 중 DB오류 발생 : ${e.message}"
                )
            }
        }
    }

    fun isExistByName(name: String): Boolean {
        log.debug("call isExistByName : name = '$name'")

        return try {
            pgRepository.existsByName(name)
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
            pgRepository.existsById(id)
        } catch (e: Exception) {
            log.error("isExistByName DB search failed. $id", e)
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_DB,
                loglevel = Level.ERROR,
                message = "isExistByName 호출 중 DB오류 발생 : ${e.message}"
            )
        }
    }
}