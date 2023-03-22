package com.example.tobi.integrated.db.repository

import com.example.tobi.integrated.db.entity.Bundle
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface BundleRepository : JpaRepository<Bundle, Long> {
    fun findByUserIdAndPaidAtIsNull(userId: Long): Optional<Bundle>
}