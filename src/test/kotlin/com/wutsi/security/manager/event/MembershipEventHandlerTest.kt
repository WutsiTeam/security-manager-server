package com.wutsi.security.manager.event

import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.membership.manager.event.EventURN
import com.wutsi.membership.manager.event.MemberEventPayload
import com.wutsi.platform.core.messaging.MessagingType
import com.wutsi.platform.core.stream.Event
import com.wutsi.security.manager.dto.CreateOTPRequest
import com.wutsi.security.manager.dto.CreatePasswordRequest
import com.wutsi.security.manager.entity.OtpEntity
import com.wutsi.security.manager.entity.PasswordEntity
import com.wutsi.security.manager.service.OtpService
import com.wutsi.security.manager.service.PasswordService
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
        // GIVEN
        val otp = OtpEntity()
        doReturn(otp).whenever(otpService).create(any())

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
        // GIVEN
        val password = PasswordEntity(id = 34909)
        doReturn(password).whenever(passwordService).create(any())

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

    @Test
    fun onMemberDeleted() {
        // WHEN
        val payload = MemberEventPayload(
            accountId = 111L
        )
        val event = Event(
            type = EventURN.MEMBER_DELETED.urn,
            payload = mapper.writeValueAsString(payload)
        )
        handler.onMemberDeleted(event)

        // THEN
        verify(passwordService).deleteByAccountId(payload.accountId)
    }
}
