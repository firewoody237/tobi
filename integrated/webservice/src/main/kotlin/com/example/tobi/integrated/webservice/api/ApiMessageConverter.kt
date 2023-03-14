package com.example.tobi.integrated.webservice.api

import com.example.tobi.integrated.common.resultcode.ResultCode
import org.springframework.http.HttpInputMessage
import org.springframework.http.HttpOutputMessage
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.http.server.ServletServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerMapping

@Component
class ApiMessageConverter(
    private val mappingJackson2HttpMessageConverter: MappingJackson2HttpMessageConverter
) : HttpMessageConverter<Any> {

    override fun canRead(clazz: Class<*>, mediaType: MediaType?) = false
    override fun read(clazz: Class<out Any>, inputMessage: HttpInputMessage) = throw Exception("의도하지 않은 동작")

    override fun getSupportedMediaTypes() = listOf(MediaType("*"))

    //ApiRequestMapping의 경우 무조건 여기서 쓴다
    override fun canWrite(clazz: Class<*>, mediaType: MediaType?): Boolean {
        val handlerMethod = ((RequestContextHolder.getRequestAttributes() ?: return false)
            .getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE, 0) ?: return false) as HandlerMethod
        return handlerMethod.hasMethodAnnotation(ApiRequestMapping::class.java)
    }


    //무조건 200 OK에 application_json 방식, 모두 Response객체로 감싸져서 나간다
    override fun write(originResponse: Any, contentType: MediaType?, outputMessage: HttpOutputMessage) {
        (outputMessage as ServletServerHttpResponse).setStatusCode(HttpStatus.OK)
        val result = when (originResponse) {
            is Response -> originResponse
            is ResultCode -> Response(originResponse)
            else -> Response(ResultCode.SUCCESS, response = originResponse)
        }
        mappingJackson2HttpMessageConverter.write(result, MediaType.APPLICATION_JSON, outputMessage)
    }
}