package com.example.tobi.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.ServletComponentScan


@SpringBootApplication(scanBasePackages = ["com.example.tobi"])
@ServletComponentScan(basePackages = ["com.example.tobi"])
class ApiApplication {

}

fun main(args: Array<String>) {
    runApplication<ApiApplication>(*args)
}