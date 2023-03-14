package com.example.tobi.integrated.webservice.logging

import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.annotation.WebFilter
import javax.servlet.http.HttpServletRequest
import org.apache.commons.lang.StringUtils
import org.apache.logging.log4j.ThreadContext
import java.util.*

@WebFilter(urlPatterns = ["/*"])
class ThreadContextLoggingFilter : Filter {
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        if (request is HttpServletRequest) {
            //로그 확인을 위함 response에 추가되어 나간다
            ThreadContext.put(
                "requestUuid",
                request.getHeader("requestUuid") ?: UUID.randomUUID().toString().replace("-", "")
            )
            ThreadContext.put("userAgent", request.getHeader("userAgent"))
            ThreadContext.put("clientIp", request.remoteAddr)
            ThreadContext.put(
                "requestUrl",
                arrayOf(request.requestURI, request.queryString).filter(StringUtils::isNotBlank).joinToString("?")
            )
        }
        chain.doFilter(request, response)
    }
}