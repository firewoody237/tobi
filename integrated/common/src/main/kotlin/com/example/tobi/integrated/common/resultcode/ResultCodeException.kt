package com.example.tobi.integrated.common.resultcode

import org.apache.commons.lang.StringUtils
import org.apache.logging.log4j.Level

class ResultCodeException(
    val resultCode: ResultCode,
    val loglevel: Level = Level.FATAL,
    message: Any? = null,
    val response: Any? = null,
    throwable: Throwable? = null,
    val logMessage: String? = null
) : Exception(message?.toString(), throwable) {
    constructor(resultCode: ResultCode, e: Exception) : this(resultCode, throwable = e)

    val originMessage: String? = message?.toString()

    override val message: String
        get() = if (StringUtils.isNotBlank(super.message)) {
            "[${resultCode.code}]${resultCode.msg} - ${super.message}"
        } else {
            "[${resultCode.code}]${resultCode.msg}"
        }
}