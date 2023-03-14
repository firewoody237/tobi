package com.example.tobi.integrated.db.repository

import com.example.tobi.integrated.db.entity.PG
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface PGRepository : JpaRepository<PG, Long>{
    fun existsByName(name: String): Boolean
}