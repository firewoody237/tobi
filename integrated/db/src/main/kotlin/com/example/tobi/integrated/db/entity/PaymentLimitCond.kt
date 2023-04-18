package com.example.tobi.integrated.db.entity

import org.springframework.data.jpa.domain.support.AuditingEntityListener
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.persistence.Table

@Entity
@EntityListeners(value = [AuditingEntityListener::class])
@Table(name = "payment_limit_cond")
data class PaymentLimitCond(
    @Id
    @GeneratedValue
    val id: Long = 0L,
    @OneToOne
    @JoinColumn
    var pg: PG? = null,
    @Column
    var transactionLimit: Long? = null,
    @Column
    var dailyLimit: Long? = null,
    @Column
    var monthlyLimit: Long? = null,

) : BaseTime() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PaymentLimitCond

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "Bundle(id=$id, pg='$pg', transactionLimit='$transactionLimit', dailyLimit='$dailyLimit', monthlyLimit='$monthlyLimit')"
    }
}
