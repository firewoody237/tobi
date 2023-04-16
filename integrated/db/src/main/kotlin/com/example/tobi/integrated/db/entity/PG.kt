package com.example.tobi.integrated.db.entity

import org.springframework.data.jpa.domain.support.AuditingEntityListener
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
@Table(name = "pg")
data class PG(
    @Id
    @GeneratedValue
    val id: Long = 0L,
    @Column
    var name: String? = "",
    @ManyToOne
    @JoinColumn
    var pgCode: PGCode? = null
) : BaseTime() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PG

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "Package(id=$id, name='$name', pgCode='$pgCode')"
    }

}
