package com.example.tobi.integrated.webservice.api

import com.example.tobi.integrated.common.resultcode.ResultCode
import com.example.tobi.integrated.common.resultcode.ResultCodeException
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.springframework.boot.json.JsonParseException
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.http.server.ServletServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.method.HandlerMethod
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.HandlerExceptionResolver
import org.springframework.web.servlet.ModelAndView
import java.lang.reflect.InvocationTargetException

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class ApiExceptionResolver(
    val apiMessageConverter: ApiMessageConverter
) : HandlerExceptionResolver {

    companion object {
        private val log = LogManager.getLogger()
    }

    override fun resolveException(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any?,
        exception: Exception
    ): ModelAndView? {
        val handlerMethod = (handler ?: return null) as? HandlerMethod ?: return null
        if (!handlerMethod.hasMethodAnnotation(ApiRequestMapping::class.java)) {
            return null
        }

        var throwable: Throwable = exception
        if (throwable is ServletException) {
            throwable = throwable.rootCause ?: throwable
        }
        if (throwable is InvocationTargetException) {
            throwable = throwable.targetException ?: throwable
        }

        val (level, result) = when (throwable) {
            is HttpMessageNotReadableException -> Pair(
                Level.WARN,
                when (throwable.cause) {
                    is JsonParseException -> Response(ResultCode.ERROR_PARAMETER_JSON_PARSING)
                    else -> Response(ResultCode.ERROR_HTTP_BODY)
                }
            )

            is HttpMediaTypeNotSupportedException -> Pair(
                Level.WARN,
                Response(
                    ResultCode.ERROR_NOT_MEDIA_TYPE,
                    msg = "다음중 하나로 요청 부탁드립니다 : ${throwable.supportedMediaTypes.filter { !(it.type == "*" && it.subtype == "*") }}"
                )
            )

            is MissingServletRequestParameterException -> Pair(
                Level.WARN,
                Response(ResultCode.ERROR_PARAMETER_NOT_EXISTS, msg = throwable.parameterName)
            )

            is MethodArgumentTypeMismatchException -> Pair(
                Level.WARN,
                Response(
                    ResultCode.ERROR_PARAMETER_TYPE,
                    msg = "파라미터 : ${throwable.name}, 필요타입 : ${throwable.requiredType}"
                )
            )

            is HttpRequestMethodNotSupportedException -> Pair(
                Level.WARN,
                Response(ResultCode.ERROR_NOT_SUPPORTED_HTTP_METHOD)
            )

            is AccessDeniedException -> Pair(
                Level.WARN,
                Response(ResultCode.ERROR_ACCESS_DENIED)
            )

            is ResultCodeException -> Pair(
                throwable.loglevel,
                Response(throwable.resultCode, msg = throwable.originMessage, response = throwable.response)
            )

            else -> {
                log.warn("분류되지 않는 ApiException", throwable)
                return null
            }
        }

        if (throwable is ResultCodeException) {
            log.log(level, "${throwable.message} (${throwable.logMessage})", throwable)
        } else {
            log.log(level, throwable.message, throwable)
        }
        apiMessageConverter.write(result, null, ServletServerHttpResponse(response))
        return ModelAndView()
    }


}