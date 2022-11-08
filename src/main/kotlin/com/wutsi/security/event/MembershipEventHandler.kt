package com.wutsi.security.event

import com.fasterxml.jackson.databind.ObjectMapper
import com.wutsi.membership.manager.event.MemberEventPayload
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
    private val mapper: ObjectMapper
) {
    fun onRegistrationStarted(event: Event) {
        val payload = toMemberPayload(event)
        if (System.currentTimeMillis() - event.timestamp.toInstant().toEpochMilli() > OtpService.OTP_TTL_MILLIS) {
            return
        }
        otpService.create(
            CreateOTPRequest(
                address = payload.phoneNumber,
                type = MessagingType.SMS.name
            )
        )
    }

    @Transactional
    fun onMemberRegistered(event: Event) {
        val payload = toMemberPayload(event)
        passwordService.create(
            CreatePasswordRequest(
                accountId = payload.accountId,
                username = payload.phoneNumber,
                value = payload.pin!!
            )
        )
    }

    @Transactional
    fun onMemberDeleted(event: Event) {
        val payload = toMemberPayload(event)
        passwordService.deleteByAccountId(payload.accountId)
    }

    private fun toMemberPayload(event: Event): MemberEventPayload =
        mapper.readValue(event.payload, MemberEventPayload::class.java)
}
