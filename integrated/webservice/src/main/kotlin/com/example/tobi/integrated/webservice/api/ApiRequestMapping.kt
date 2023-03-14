package com.example.tobi.integrated.webservice.api

import org.springframework.core.annotation.AliasFor
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody

@Target(AnnotationTarget.FUNCTION)
@RequestMapping
@ResponseBody
annotation class ApiRequestMapping(
    @get:AliasFor(annotation = RequestMapping::class, attribute = "value")
    val value: String = "",
    val method: Array<RequestMethod> = []
)