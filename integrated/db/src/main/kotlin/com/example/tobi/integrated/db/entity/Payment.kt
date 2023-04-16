package com.example.tobi.integrated.db.entity

import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@EntityListeners(value = [AuditingEntityListener::class])
@Table(name = "payment")
data class Payment(
    @Id
    @GeneratedValue
    val id: Long = 0L,
    @ManyToOne
    @JoinColumn
    var pg: PG? = null,
    @Column
    var paidAt: LocalDateTime? = null,
    @Column
    var amount: Long? = 0L,
    @Column
    var approveNo: String? = null,
    @Column
    var approveAt: String? = null,
    @ManyToOne
    @JoinColumn
    val pkg: Package? = null,
    @Column
    var originalPaymentId: Long? = null,
) : BaseTime() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Payment

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "Package(id=$id, pg='$pg', approveNo='$approveNo', paidAt='$paidAt', amount='$amount', approveAt='$approveAt', pkg='$pkg', originalPaymentId='$originalPaymentId')"
    }
}
