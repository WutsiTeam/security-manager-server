package com.wutsi.security.service

import com.auth0.jwt.JWT
import com.wutsi.platform.core.error.Error
import com.wutsi.platform.core.error.exception.ConflictException
import com.wutsi.platform.core.error.exception.ForbiddenException
import com.wutsi.platform.core.error.exception.NotFoundException
import com.wutsi.platform.core.messaging.MessagingType
import com.wutsi.platform.core.security.SubjectType
import com.wutsi.platform.core.security.TokenBlacklistService
import com.wutsi.platform.core.security.spring.jwt.JWTBuilder
import com.wutsi.security.dto.CreateOTPRequest
import com.wutsi.security.dto.LoginRequest
import com.wutsi.security.dto.VerifyOTPRequest
import com.wutsi.security.entity.OtpEntity
import com.wutsi.security.error.ErrorURN
import org.springframework.stereotype.Service

@Service
class AuthenticationService(
    private val otpService: OtpService,
    private val passwordService: PasswordService,
    private val keyProvider: RSAKeyProviderImpl,
    private val blacklistService: TokenBlacklistService
) {
    companion object {
        const val USER_TOKEN_TTL_MILLIS = 84600000L // 1 day
    }

    fun login(request: LoginRequest): String {
        if (request.mfaToken.isEmpty()) {
            val otp = send(request)
            throw ForbiddenException(
                error = Error(
                    code = ErrorURN.AUTHENTICATION_MFA_REQUIRED.urn,
                    data = mapOf(
                        "mfaToken" to otp.token
                    )
                )
            )
        } else {
            return verify(request)
        }
    }

    fun logout(accessToken: String) {
        val jwt = JWT.decode(accessToken)
        val ttl = (jwt.expiresAt.time - System.currentTimeMillis()) / 1000
        if (ttl > 0) {
            blacklistService.add(accessToken, ttl)
        }
    }

    private fun send(request: LoginRequest): OtpEntity {
        val password = passwordService.findByUsername(request.phoneNumber)
            .orElseThrow {
                NotFoundException(
                    error = Error(
                        code = ErrorURN.PASSWORD_NOT_FOUND.urn
                    )
                )
            }
        val otpRequest = CreateOTPRequest(
            address = password.username,
            type = MessagingType.SMS.name
        )
        val otp = otpService.create(otpRequest)
        otpService.send(otpRequest, otp)

        return otp
    }

    private fun verify(request: LoginRequest): String {
        val otp = otpService.verify(
            token = request.mfaToken,
            request = VerifyOTPRequest(
                code = request.verificationCode
            )
        )

        val password = passwordService.findByUsername(otp.address)
            .orElseThrow {
                ConflictException(
                    error = Error(
                        code = ErrorURN.PASSWORD_NOT_FOUND.urn
                    )
                )
            }

        return JWTBuilder(
            ttl = USER_TOKEN_TTL_MILLIS,
            subjectType = SubjectType.USER,
            name = otp.address,
            subject = password.accountId.toString(),
            keyProvider = keyProvider
        ).build()
    }
}
