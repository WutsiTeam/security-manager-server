package com.wutsi.security.`delegate`

import com.wutsi.security.dto.LoginRequest
import com.wutsi.security.dto.LoginResponse
import com.wutsi.security.service.AuthenticationService
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
public class LoginDelegate(private val service: AuthenticationService) {
    @Transactional
    public fun invoke(request: LoginRequest): LoginResponse =
        LoginResponse(
            accessToken = service.login(request)
        )
}
