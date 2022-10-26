package com.wutsi.security.`delegate`

import com.wutsi.platform.core.error.Error
import com.wutsi.platform.core.error.exception.BadRequestException
import com.wutsi.security.error.ErrorURN
import com.wutsi.security.service.AuthenticationService
import org.springframework.stereotype.Service
import javax.servlet.http.HttpServletRequest

@Service
class LogoutDelegate(
    private val service: AuthenticationService,
    private val request: HttpServletRequest
) {
    companion object {
        const val AUTHORIZATION_PREFIX = "Bearer "
    }

    fun invoke() {
        val authorization = request.getHeader("Authorization")
        if (authorization?.startsWith(AUTHORIZATION_PREFIX, true) != true) {
            throw BadRequestException(
                error = Error(
                    code = ErrorURN.AUTHORIZATION_HEADER_MISSING.urn
                )
            )
        }
        val accessToken = authorization.substring(AUTHORIZATION_PREFIX.length).trim()
        service.logout(accessToken)
    }
}
