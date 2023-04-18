package com.example.tobi.integrated.db.entity

import org.springframework.data.jpa.domain.support.AuditingEntityListener
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table

@Entity
@EntityListeners(value = [AuditingEntityListener::class])
@Table(name = "bundle")
data class Bundle(
    @Id
    @GeneratedValue
    val id: Long = 0L,
    @Column
    var userId: Long? = null,
    @Column
    var amount: Long = 0L,
) : BaseTime() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Bundle

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "Bundle(id=$id, userId='$userId', amount='$amount')"
    }
}
