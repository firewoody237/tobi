package com.example.tobi.integrated.db.repository

import com.example.tobi.integrated.db.entity.PGCode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface PGCodeRepository : JpaRepository<PGCode, Long> {
    fun existsByName(name: String): Boolean
    fun existsByPgId(pgId: String): Boolean
}