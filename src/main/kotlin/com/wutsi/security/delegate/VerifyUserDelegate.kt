package com.wutsi.security.`delegate`

import com.wutsi.security.dto.VerifyUserRequest
import org.springframework.stereotype.Service
import kotlin.Long

@Service
public class VerifyUserDelegate() {
    public fun invoke(username: Long, request: VerifyUserRequest) {
        TODO()
    }
}
