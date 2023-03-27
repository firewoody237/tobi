package com.example.tobi.integrated.db.repository

import com.example.tobi.integrated.db.entity.Bundle
import com.example.tobi.integrated.db.entity.Package
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PackageRepository : JpaRepository<Package, Long> {

    fun findByBundle(bundle: Bundle): MutableList<Package>
    fun findByBundleAndItemId(bundle: Bundle, itemId: Long): Package
}