package com.wutsi.security.`delegate`

import com.wutsi.security.dto.GetKeyResponse
import org.springframework.stereotype.Service
import kotlin.Long

@Service
public class GetKeyDelegate() {
    public fun invoke(id: Long): GetKeyResponse {
        TODO()
    }
}
