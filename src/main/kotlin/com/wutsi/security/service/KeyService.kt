package com.wutsi.security.service

import com.wutsi.platform.core.error.Error
import com.wutsi.platform.core.error.Parameter
import com.wutsi.platform.core.error.ParameterType
import com.wutsi.platform.core.error.exception.NotFoundException
import com.wutsi.security.dao.KeyRepository
import com.wutsi.security.entity.KeyEntity
import com.wutsi.security.error.ErrorURN
import org.springframework.stereotype.Service

@Service
class KeyService(private val dao: KeyRepository) {
    fun findById(id: Long): KeyEntity {
        val key = dao.findById(id)
            .orElseThrow {
                NotFoundException(
                    error = Error(
                        code = ErrorURN.KEY_NOT_FOUND.urn,
                        parameter = Parameter(
                            name = "id",
                            value = id.toString(),
                            type = ParameterType.PARAMETER_TYPE_PATH
                        )
                    )
                )
            }

        if (expired(key)) {
            throw NotFoundException(
                error = Error(
                    code = ErrorURN.KEY_EXPIRED.urn,
                    parameter = Parameter(
                        name = "id",
                        value = id.toString(),
                        type = ParameterType.PARAMETER_TYPE_PATH
                    )
                )
            )
        }

        return key
    }

    private fun expired(key: KeyEntity): Boolean =
        key.expires.time < System.currentTimeMillis()
}
