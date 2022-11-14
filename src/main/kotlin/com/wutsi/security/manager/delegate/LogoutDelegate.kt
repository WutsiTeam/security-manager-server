package com.wutsi.security.manager.delegate

import com.wutsi.platform.core.error.Error
import com.wutsi.platform.core.error.exception.BadRequestException
import com.wutsi.platform.core.tracing.TracingContext
import com.wutsi.security.manager.error.ErrorURN
import com.wutsi.security.manager.service.LoginService
import org.springframework.stereotype.Service
import javax.servlet.http.HttpServletRequest
import javax.transaction.Transactional

@Service
class LogoutDelegate(
    private val service: LoginService,
    private val request: HttpServletRequest,
    private val tracingContext: TracingContext
) {
    companion object {
        const val AUTHORIZATION_PREFIX = "Bearer "
    }

    @Transactional
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
        val login = service.logout(accessToken)
        if (login != null) {
            service.logoutPreviousSession(login, tracingContext.traceId())
        }
    }
}
