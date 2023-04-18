package com.example.tobi.integrated.db.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PGCodeRepository : JpaRepository<PGCode, Long> {
    fun existsByName(name: String): Boolean
    fun existsByPgId(pgId: String): Boolean
}