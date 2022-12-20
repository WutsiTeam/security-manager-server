package com.wutsi.security.manager.event

import com.fasterxml.jackson.databind.ObjectMapper
import com.wutsi.event.MemberEventPayload
import com.wutsi.platform.core.logging.KVLogger
import com.wutsi.platform.core.stream.Event
import com.wutsi.security.manager.dto.CreatePasswordRequest
import com.wutsi.security.manager.service.OtpService
import com.wutsi.security.manager.service.PasswordService
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class MembershipEventHandler(
    private val otpService: OtpService,
    private val passwordService: PasswordService,
    private val mapper: ObjectMapper,
    private val logger: KVLogger,
) {
    @Transactional
    fun onMemberRegistered(event: Event) {
        val payload = toMemberPayload(event)
        log(payload)

        val password = passwordService.create(
            CreatePasswordRequest(
                accountId = payload.accountId,
                username = payload.phoneNumber,
                value = payload.pin!!,
            ),
        )
        logger.add("password_id", password.id)
    }

    @Transactional
    fun onMemberDeleted(event: Event) {
        val payload = toMemberPayload(event)
        log(payload)

        passwordService.delete(payload.accountId)
    }

    private fun toMemberPayload(event: Event): MemberEventPayload =
        mapper.readValue(event.payload, MemberEventPayload::class.java)

    private fun log(payload: MemberEventPayload) {
        logger.add("payload_account_id", payload.accountId)
        logger.add("payload_phone_number", payload.phoneNumber)
        logger.add("payload_pin", "*********")
    }
}
