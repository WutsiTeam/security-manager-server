package com.wutsi.security.`delegate`

import com.wutsi.security.dto.LoginRequest
import com.wutsi.security.dto.LoginResponse
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
public class LoginDelegate() {
    @Transactional
    public fun invoke(request: LoginRequest): LoginResponse {
        TODO()
    }
}
