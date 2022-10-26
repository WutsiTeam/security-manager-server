package com.wutsi.security.endpoint

import com.auth0.jwt.JWT
import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.platform.core.error.ErrorResponse
import com.wutsi.platform.core.messaging.Message
import com.wutsi.platform.core.messaging.MessagingService
import com.wutsi.platform.core.messaging.MessagingServiceProvider
import com.wutsi.platform.core.messaging.MessagingType
import com.wutsi.platform.core.security.SubjectType
import com.wutsi.platform.core.security.spring.jwt.JWTBuilder
import com.wutsi.security.dao.OtpRepository
import com.wutsi.security.dto.LoginRequest
import com.wutsi.security.dto.LoginResponse
import com.wutsi.security.entity.OtpEntity
import com.wutsi.security.error.ErrorURN
import com.wutsi.security.service.LoginService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.jdbc.Sql
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(value = ["/db/clean.sql", "/db/LoginController.sql"])
class LoginControllerTest {
    @LocalServerPort
    val port: Int = 0

    protected val rest = RestTemplate()

    @Autowired
    private lateinit var otpDao: OtpRepository

    @Autowired
    private lateinit var loginService: LoginService

    @MockBean
    private lateinit var messagingServiceProvider: MessagingServiceProvider

    private lateinit var messaging: MessagingService

    @BeforeEach
    fun setUp() {
        messaging = mock()
        doReturn(messaging).whenever(messagingServiceProvider).get(MessagingType.SMS)
        doReturn("1111").whenever(messaging).send(any())
    }

    @Test
    fun `send MFA token`() {
        // WHEN
        val request = LoginRequest(
            phoneNumber = "+1237670000000"
        )
        val ex = assertThrows<HttpClientErrorException> {
            rest.postForEntity(url(), request, LoginResponse::class.java)
        }

        // THEN
        assertEquals(HttpStatus.FORBIDDEN, ex.statusCode)

        val response = ObjectMapper().readValue(ex.responseBodyAsString, ErrorResponse::class.java)
        assertEquals(ErrorURN.AUTHENTICATION_MFA_REQUIRED.urn, response.error.code)

        val msg = argumentCaptor<Message>()
        verify(messaging).send(msg.capture())
        assertEquals(request.phoneNumber, msg.firstValue.recipient.phoneNumber)

        val token = response.error.data!!["mfaToken"].toString()
        val otp = otpDao.findById(token)
        assertTrue(otp.isPresent)
        assertEquals(request.phoneNumber, otp.get().address)
    }

    @Test
    fun `dont send MFA token to deleted account`() {
        // WHEN
        val request = LoginRequest(
            phoneNumber = "+1237670000099"
        )
        val ex = assertThrows<HttpClientErrorException> {
            rest.postForEntity(url(), request, LoginResponse::class.java)
        }

        // THEN
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)

        val response = ObjectMapper().readValue(ex.responseBodyAsString, ErrorResponse::class.java)
        assertEquals(ErrorURN.PASSWORD_NOT_FOUND.urn, response.error.code)
    }

    @Test
    fun `dont send MFA token to invalid account`() {
        // WHEN
        val request = LoginRequest(
            phoneNumber = "invalid-account"
        )
        val ex = assertThrows<HttpClientErrorException> {
            rest.postForEntity(url(), request, LoginResponse::class.java)
        }

        // THEN
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)

        val response = ObjectMapper().readValue(ex.responseBodyAsString, ErrorResponse::class.java)
        assertEquals(ErrorURN.PASSWORD_NOT_FOUND.urn, response.error.code)
    }

    @Test
    fun `authentication`() {
        // GIVEN
        val otp = OtpEntity(
            token = UUID.randomUUID().toString(),
            code = "123456",
            expires = System.currentTimeMillis() + 900000,
            address = "+1237670000000"
        )
        otpDao.save(otp)

        // WHEN
        val request = LoginRequest(
            mfaToken = otp.token,
            verificationCode = otp.code
        )
        val response = rest.postForEntity(url(), request, LoginResponse::class.java)

        // THEN
        assertEquals(HttpStatus.OK, response.statusCode)

        val accessToken = response.body?.accessToken
        assertNotNull(accessToken)
        val decoded = JWT.decode(accessToken)
        assertEquals("100", decoded.subject)
        assertNotNull(decoded.keyId)
        assertEquals(SubjectType.USER.name, decoded.claims[JWTBuilder.CLAIM_SUBJECT_TYPE]?.asString())
        assertEquals(otp.address, decoded.claims[JWTBuilder.CLAIM_NAME]?.asString())
        assertEquals(
            LoginService.USER_TOKEN_TTL_MILLIS / 60000,
            (decoded.expiresAt.time - decoded.issuedAt.time) / 60000
        )

        val login = loginService.findByAccessToken(accessToken)
        assertTrue(login.isPresent)
        assertEquals(100L, login.get().accountId)
        assertNotNull(login.get().expires)
        assertNotNull(login.get().created)
        assertNull(login.get().expired)
        assertEquals(accessToken, login.get().accessToken)
    }

    @Test
    fun `authentication fails with invalid code`() {
        // GIVEN
        val otp = OtpEntity(
            token = UUID.randomUUID().toString(),
            code = "123456",
            expires = System.currentTimeMillis() + 900000,
            address = "+1237670000000"
        )
        otpDao.save(otp)

        // WHEN
        val request = LoginRequest(
            mfaToken = otp.token,
            verificationCode = "this-is-an-invalid-code"
        )
        val ex = assertThrows<HttpClientErrorException> {
            rest.postForEntity(url(), request, LoginResponse::class.java)
        }

        // THEN
        assertEquals(HttpStatus.CONFLICT, ex.statusCode)

        val response = ObjectMapper().readValue(ex.responseBodyAsString, ErrorResponse::class.java)
        assertEquals(ErrorURN.OTP_NOT_VALID.urn, response.error.code)
    }

    private fun url() = "http://localhost:$port/v1/auth"
}
