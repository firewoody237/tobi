package com.example.tobi.integrated.db.repository

import com.example.tobi.integrated.db.entity.Bundle
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

@Repository
interface BundleRepository : JpaRepository<Bundle, Long> {
    fun findByUserId(userId: Long): Optional<Bundle>
    fun findByUserIdAndCreatedAtBetween(userId: Long, startOfDay: LocalDateTime, endOfDay: LocalDateTime): MutableList<Bundle>

    fun findTodayBundles(userId: Long, date: LocalDateTime): MutableList<Bundle> {
        val startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN)
        val endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX)
        return findByUserIdAndCreatedAtBetween(userId, startOfDay, endOfDay)
    }

    fun findThisMonthBundles(userId: Long, date: LocalDateTime): MutableList<Bundle> {
        val now = LocalDateTime.now()
        val startOfMonth = LocalDateTime.of(LocalDate.now().withDayOfMonth(1), LocalTime.MIN)
        val endOfMonth = LocalDateTime.of(LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()), LocalTime.MAX)
        return findByUserIdAndCreatedAtBetween(userId, startOfMonth, endOfMonth)
    }
}