package com.wutsi.security.`delegate`

import com.wutsi.security.dto.GetKeyResponse
import com.wutsi.security.dto.Key
import com.wutsi.security.service.KeyService
import org.springframework.stereotype.Service

@Service
public class GetKeyDelegate(private val service: KeyService) {
    public fun invoke(id: Long): GetKeyResponse {
        val key = service.findById(id)
        return GetKeyResponse(
            key = Key(
                id = key.id!!,
                algorithm = key.algorithm,
                content = key.publicKey
            )
        )
    }
}
