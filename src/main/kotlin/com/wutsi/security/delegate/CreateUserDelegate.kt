package com.wutsi.security.`delegate`

import com.wutsi.security.dto.CreateUserRequest
import com.wutsi.security.dto.CreateUserResponse
import org.springframework.stereotype.Service

@Service
public class CreateUserDelegate() {
    public fun invoke(request: CreateUserRequest): CreateUserResponse {
        TODO()
    }
}
