package com.example.tobi.integrated.tomcat

import org.apache.coyote.AbstractProtocol
import org.apache.coyote.ajp.AbstractAjpProtocol
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory
import org.springframework.boot.web.server.Shutdown
import org.springframework.boot.web.servlet.server.ServletWebServerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class Tomcat {
    @Bean
    fun servletController(
        @Value("\${server.tomcat.shutdown}")
        shutdown: Shutdown,
        @Value("\${server.tomcat.protocol}")
        protocol: String,
        @Value("\${server.tomcat.port}")
        port: Int,
        @Value("\${server.tomcat.maxThreads}")
        maxThreads: Int,
        @Value("\${server.tomcat.minSpareThreads}")
        minSpareThreads: Int,
        @Value("\${server.tomcat.secretRequired}")
        secretRequired: Boolean,
        @Value("\${server.tomcat.useBodyEncodingForURI}")
        useBodyEncodingForURI: Boolean,
        @Value("\${server.tomcat.enableLookups}")
        enableLookups: Boolean,
        @Value("\${server.tomcat.maxParameterCount}")
        maxParameterCount: Int,
        @Value("\${server.tomcat.maxPostSize}")
        maxPostSize: Int,

        ): ServletWebServerFactory {
        return TomcatServletWebServerFactory().apply {
            setShutdown(shutdown)
            setProtocol(protocol)
            setPort(port)
            addProtocolHandlerCustomizers({
                if (it is AbstractProtocol<*>) {
                    it.maxThreads = maxThreads
                    it.minSpareThreads = minSpareThreads
                }
                if (it is AbstractAjpProtocol<*>) {
                    it.secretRequired = secretRequired
                }
            })

            addConnectorCustomizers({
                it.useBodyEncodingForURI = useBodyEncodingForURI
                it.enableLookups = enableLookups
                it.maxParameterCount = maxParameterCount
                it.maxPostSize = maxPostSize
            })
        }
    }
}