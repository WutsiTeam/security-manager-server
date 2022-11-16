package com.wutsi.security.manager.service

import com.wutsi.platform.core.error.Error
import com.wutsi.platform.core.error.Parameter
import com.wutsi.platform.core.error.ParameterType
import com.wutsi.platform.core.error.exception.ConflictException
import com.wutsi.platform.core.error.exception.NotFoundException
import com.wutsi.security.manager.dao.PasswordRepository
import com.wutsi.security.manager.dto.CreatePasswordRequest
import com.wutsi.security.manager.dto.UpdatePasswordRequest
import com.wutsi.security.manager.dto.VerifyPasswordRequest
import com.wutsi.security.manager.entity.PasswordEntity
import com.wutsi.security.manager.error.ErrorURN
import org.apache.commons.codec.digest.DigestUtils
import org.springframework.stereotype.Service
import java.util.Date
import java.util.UUID

@Service
class PasswordService(
    private val dao: PasswordRepository
) {
    fun create(request: CreatePasswordRequest): PasswordEntity {
        val salt = UUID.randomUUID().toString()
        return dao.save(
            PasswordEntity(
                accountId = request.accountId,
                username = request.username,
                value = hash(request.accountId, request.value, salt),
                salt = salt
            )
        )
    }

    fun update(id: Long, request: UpdatePasswordRequest) {
        val password = findById(id)
        password.value = hash(password.accountId, request.value, password.salt)
        dao.save(password)
    }

    fun verify(request: VerifyPasswordRequest): PasswordEntity {
        val password = findByUsername(request.username)
        val value = hash(password.accountId, request.value, password.salt)
        if (value != password.value) {
            throw ConflictException(
                error = Error(
                    code = ErrorURN.PASSWORD_MISMATCH.urn
                )
            )
        }
        return password
    }

    fun delete(id: Long) {
        val password = dao.findById(id)
        if (password.isPresent) {
            delete(password.get())
        }
    }

    fun deleteByAccountId(accountId: Long) {
        val password = dao.findByAccountIdAndIsDeleted(accountId, false)
        password.forEach {
            delete(it)
        }
    }

    private fun delete(password: PasswordEntity) {
        if (!password.isDeleted) {
            password.isDeleted = true
            password.deleted = Date()
            dao.save(password)
        }
    }

    fun findById(id: Long): PasswordEntity {
        val password = dao.findById(id)
            .orElseThrow {
                notFound(id)
            }
        if (password.isDeleted) {
            throw notFound(id)
        }
        return password
    }

    fun findByUsername(username: String): PasswordEntity {
        val passwords = dao.findByUsernameAndIsDeleted(username, false)
        if (passwords.isEmpty()) {
            throw NotFoundException(
                error = Error(
                    code = ErrorURN.PASSWORD_NOT_FOUND.urn,
                    data = mapOf(
                        "username" to username
                    )
                )
            )
        } else {
            return passwords[0]
        }
    }

    private fun notFound(id: Long) = NotFoundException(
        error = Error(
            code = ErrorURN.PASSWORD_NOT_FOUND.urn,
            parameter = Parameter(
                name = "id",
                type = ParameterType.PARAMETER_TYPE_PATH,
                value = id.toString()
            )
        )
    )

    private fun hash(accountId: Long, value: String, salt: String): String =
        DigestUtils.md5Hex("$accountId-$value-$salt")
}
