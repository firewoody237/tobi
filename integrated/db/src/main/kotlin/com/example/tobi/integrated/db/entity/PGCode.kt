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
@Table(name = "pgcode")
data class PGCode(
    @Id
    @GeneratedValue
    val id: Long = 0L,
    @Column
    var name: String? = "",
    @Column
    var pgId: String? = null,
) : BaseTime() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PGCode

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "Package(id=$id, name='$name', pgId='$pgId')"
    }
}
