package com.wutsi.security.event

import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.wutsi.membership.manager.event.EventURN
import com.wutsi.membership.manager.event.MemberEventPayload
import com.wutsi.platform.core.messaging.MessagingType
import com.wutsi.platform.core.stream.Event
import com.wutsi.security.dto.CreateOTPRequest
import com.wutsi.security.dto.CreatePasswordRequest
import com.wutsi.security.service.OtpService
import com.wutsi.security.service.PasswordService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import java.time.OffsetDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class MembershipEventHandlerTest {
    @MockBean
    private lateinit var otpService: OtpService

    @MockBean
    private lateinit var passwordService: PasswordService

    @Autowired
    private lateinit var handler: MembershipEventHandler

    @Autowired
    private lateinit var mapper: ObjectMapper

    @Test
    fun onRegistrationStarted() {
        // WHEN
        val payload = MemberEventPayload(
            phoneNumber = "+237670000010"
        )
        val event = Event(
            type = EventURN.MEMBER_REGISTRATION_STARTED.urn,
            payload = mapper.writeValueAsString(payload)
        )
        handler.onRegistrationStarted(event)

        // THEN
        verify(otpService).create(
            CreateOTPRequest(
                address = payload.phoneNumber,
                type = MessagingType.SMS.name
            )
        )
    }

    @Test
    fun onRegistrationStartedExpired() {
        // WHEN
        val payload = MemberEventPayload(
            phoneNumber = "+237670000010"
        )
        val event = Event(
            type = EventURN.MEMBER_REGISTRATION_STARTED.urn,
            payload = mapper.writeValueAsString(payload),
            timestamp = OffsetDateTime.now().minusMinutes(10)
        )
        handler.onRegistrationStarted(event)

        // THEN
        verify(otpService, never()).create(any())
    }

    @Test
    fun onMemberRegistered() {
        // WHEN
        val payload = MemberEventPayload(
            phoneNumber = "+237670000010",
            accountId = 111L,
            pin = "123456"
        )
        val event = Event(
            type = EventURN.MEMBER_REGISTERED.urn,
            payload = mapper.writeValueAsString(payload)
        )
        handler.onMemberRegistered(event)

        // THEN
        verify(passwordService).create(
            CreatePasswordRequest(
                value = payload.pin!!,
                username = payload.phoneNumber,
                accountId = payload.accountId
            )
        )
    }
}
