package com.wutsi.security.service

import com.wutsi.platform.core.error.Error
import com.wutsi.platform.core.error.Parameter
import com.wutsi.platform.core.error.ParameterType
import com.wutsi.platform.core.error.exception.BadRequestException
import com.wutsi.platform.core.error.exception.ConflictException
import com.wutsi.platform.core.messaging.Message
import com.wutsi.platform.core.messaging.MessagingService
import com.wutsi.platform.core.messaging.MessagingServiceProvider
import com.wutsi.platform.core.messaging.MessagingType
import com.wutsi.platform.core.messaging.Party
import com.wutsi.security.dao.OtpRepository
import com.wutsi.security.dto.CreateOTPRequest
import com.wutsi.security.dto.VerifyOTPRequest
import com.wutsi.security.entity.OtpEntity
import com.wutsi.security.error.ErrorURN
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Service
import java.util.UUID

@Service
public class OtpService(
    private val dao: OtpRepository,
    private val messagingProvider: MessagingServiceProvider,
    private val messageSource: MessageSource
) {
    fun create(request: CreateOTPRequest): OtpEntity =
        dao.save(
            OtpEntity(
                token = UUID.randomUUID().toString(),
                code = generateCode(6),
                expires = System.currentTimeMillis() + 900 * 1000,
                address = request.address
            )
        )

    fun send(request: CreateOTPRequest, otp: OtpEntity): String {
        val locale = LocaleContextHolder.getLocale()
        return getMessaging(request).send(
            Message(
                recipient = Party(
                    phoneNumber = request.address,
                    email = request.address,
                    deviceToken = request.address
                ),
                subject = messageSource.getMessage(
                    "verification_subject",
                    arrayOf(),
                    locale
                ),
                body = messageSource.getMessage(
                    "verification_message",
                    arrayOf(otp.code),
                    locale
                )
            )
        )
    }

    fun verify(token: String, request: VerifyOTPRequest): OtpEntity {
        val otp = dao.findById(token)
            .orElseThrow {
                ConflictException(
                    error = Error(
                        code = ErrorURN.OTP_EXPIRED.urn
                    )
                )
            }

        if (otp.expires < System.currentTimeMillis()) {
            throw ConflictException(
                error = Error(
                    code = ErrorURN.OTP_EXPIRED.urn
                )
            )
        }

        if (otp.code != request.code) {
            throw ConflictException(
                error = Error(
                    code = ErrorURN.OTP_NOT_VALID.urn
                )
            )
        }
        return otp
    }

    private fun getMessaging(request: CreateOTPRequest): MessagingService =
        when (request.type.uppercase()) {
            MessagingType.SMS.name -> messagingProvider.get(MessagingType.SMS)
            MessagingType.EMAIL.name -> messagingProvider.get(MessagingType.EMAIL)
            MessagingType.WHATSTAPP.name -> messagingProvider.get(MessagingType.WHATSTAPP)
            MessagingType.PUSH_NOTIFICATION.name -> messagingProvider.get(MessagingType.PUSH_NOTIFICATION)
            else -> throw BadRequestException(
                error = Error(
                    code = ErrorURN.OTP_ADDRESS_TYPE_NOT_VALID.urn,
                    parameter = Parameter(
                        type = ParameterType.PARAMETER_TYPE_PAYLOAD,
                        name = "type",
                        value = request.type
                    )
                )
            )
        }

    private fun generateCode(length: Int): String {
        val buff = StringBuilder()
        while (buff.length < length) {
            val digit = (Math.random() * 10).toInt()
            buff.append(digit)
        }
        return buff.toString()
    }
}
