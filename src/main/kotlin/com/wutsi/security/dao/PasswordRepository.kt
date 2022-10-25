package com.wutsi.security.dao

import com.wutsi.security.entity.PasswordEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface PasswordRepository : CrudRepository<PasswordEntity, Long>
