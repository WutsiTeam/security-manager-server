package com.wutsi.security.dao

import com.wutsi.security.entity.KeyEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface KeyRepository : CrudRepository<KeyEntity, Long> {
    fun findAll(pagination: Pageable): List<KeyEntity>
}
