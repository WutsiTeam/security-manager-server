package com.wutsi.security.`delegate`

import com.wutsi.security.dto.UpdateUserStatusRequest
import org.springframework.stereotype.Service
import kotlin.Long

@Service
public class UpdateUserStatusDelegate() {
    public fun invoke(id: Long, request: UpdateUserStatusRequest) {
        TODO()
    }
}
