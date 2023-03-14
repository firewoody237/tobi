package com.example.tobi.integrated.db.entity

import org.hibernate.cache.spi.support.AbstractReadWriteAccess.Item
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
@Table(name = "package")
data class Package(
    @Id
    @GeneratedValue
    val id: Long = 0L,
    @Column
    var title: String? = "",
    @Column
    val itemId: Long? = 0L,
    @Column
    var paidAt: LocalDateTime? = null,
    @Column
    var amount: Long? = 0L,
    @ManyToOne
    @JoinColumn
    val bundle: Bundle? = null,
) : BaseTime() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Package

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "Package(id=$id, title='$title', itemId='$itemId', paidAt='$paidAt', amount='$amount', bundle='$bundle')"
    }
}
