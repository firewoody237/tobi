package com.example.tobi.batch

import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean
import org.springframework.batch.support.transaction.ResourcelessTransactionManager
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@EnableBatchProcessing
@SpringBootApplication(scanBasePackages = ["com.example.tobi"])
class BatchApplication : DefaultBatchConfigurer() {
    @Throws(Exception::class)
    override fun createJobRepository(): JobRepository {
        val factory = MapJobRepositoryFactoryBean()
        factory.transactionManager = ResourcelessTransactionManager()
        factory.afterPropertiesSet()
        return factory.getObject()
    }
}

fun main(args: Array<String>) {
    runApplication<BatchApplication>(*args)
}