package com.example.tobi.integrated.db.repository

import com.example.tobi.integrated.db.entity.PG
import com.example.tobi.integrated.db.entity.Package
import com.example.tobi.integrated.db.entity.Payment
import com.example.tobi.integrated.db.entity.PaymentLimitCond
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface PaymentLimitCondRepository : JpaRepository<PaymentLimitCond, Long>{
    fun findByPg(pg: PG): Optional<PaymentLimitCond>
}