package com.example.tobi.integrated.db.repository

import com.example.tobi.integrated.db.entity.Package
import com.example.tobi.integrated.db.entity.Payment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PaymentRepository : JpaRepository<Payment, Long>{
    fun findByPkg(thisPackage: Package): MutableList<Payment>
    fun findByApproveNo(approveNo: String): Payment
}