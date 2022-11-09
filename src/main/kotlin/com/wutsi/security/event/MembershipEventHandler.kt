package com.wutsi.security.event

import com.fasterxml.jackson.databind.ObjectMapper
import com.wutsi.membership.manager.event.MemberEventPayload
import com.wutsi.platform.core.logging.KVLogger
import com.wutsi.platform.core.messaging.MessagingType
import com.wutsi.platform.core.stream.Event
import com.wutsi.security.dto.CreateOTPRequest
import com.wutsi.security.dto.CreatePasswordRequest
import com.wutsi.security.service.OtpService
import com.wutsi.security.service.PasswordService
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class MembershipEventHandler(
    private val otpService: OtpService,
    private val passwordService: PasswordService,
    private val mapper: ObjectMapper,
    private val logger: KVLogger
) {
    fun onRegistrationStarted(event: Event) {
        val payload = toMemberPayload(event)
        log(payload)

        if (System.currentTimeMillis() - event.timestamp.toInstant().toEpochMilli() > OtpService.OTP_TTL_MILLIS) {
            logger.add("expired", true)
            return
        }

        val request = CreateOTPRequest(
            address = payload.phoneNumber,
            type = MessagingType.SMS.name
        )
        val otp = otpService.create(request)
        logger.add("token", otp.token)
    }

    @Transactional
    fun onMemberRegistered(event: Event) {
        val payload = toMemberPayload(event)
        log(payload)

        val password = passwordService.create(
            CreatePasswordRequest(
                accountId = payload.accountId,
                username = payload.phoneNumber,
                value = payload.pin!!
            )
        )
        logger.add("password_id", password.id)
    }

    @Transactional
    fun onMemberDeleted(event: Event) {
        val payload = toMemberPayload(event)
        log(payload)

        passwordService.deleteByAccountId(payload.accountId)
    }

    private fun toMemberPayload(event: Event): MemberEventPayload =
        mapper.readValue(event.payload, MemberEventPayload::class.java)

    private fun log(payload: MemberEventPayload) {
        logger.add("payload_account_id", payload.accountId)
        logger.add("payload_phone_number", payload.phoneNumber)
        logger.add("payload_pin", "*********")
    }
}
