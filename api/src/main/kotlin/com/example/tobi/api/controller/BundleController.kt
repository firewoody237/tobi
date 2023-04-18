package com.example.tobi.api.controller

import com.example.tobi.integrated.db.dto.bundle.CancelBundleDTO
import com.example.tobi.integrated.db.dto.bundle.CreateBundleDTO
import com.example.tobi.integrated.db.service.BundleService
import com.example.tobi.integrated.webservice.api.ApiRequestMapping
import org.apache.logging.log4j.LogManager
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1")
class BundleController(
    private val bundleService: BundleService
) {
    companion object {
        private val log = LogManager.getLogger()
    }

    @ApiRequestMapping("/bundle", method = [RequestMethod.POST])
    fun createBundle(@RequestBody createBundleDTO: CreateBundleDTO): String {
        log.debug("createBundle, createBundleDTO = '$createBundleDTO'")

        return bundleService.createBundle(createBundleDTO)
    }

    @ApiRequestMapping("/bundle", method = [RequestMethod.POST])
    fun cancelBundle(@RequestBody cancelBundleDTO: CancelBundleDTO) {
        log.debug("cancelBundle, cancelBundleDTO = '$cancelBundleDTO'")

        bundleService.cancelBundle(cancelBundleDTO)
    }
}