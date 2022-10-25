package com.wutsi.security.endpoint

import com.fasterxml.jackson.databind.ObjectMapper
import com.wutsi.platform.core.error.ErrorResponse
import com.wutsi.security.dao.OtpRepository
import com.wutsi.security.dto.VerifyOTPRequest
import com.wutsi.security.entity.OtpEntity
import com.wutsi.security.error.ErrorURN
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.util.UUID
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class VerifyOtpControllerTest {
    @LocalServerPort
    public val port: Int = 0

    protected lateinit var rest: RestTemplate

    @Autowired
    private lateinit var dao: OtpRepository

    @BeforeEach
    fun setUp() {
        rest = RestTemplate()
    }

    @Test
    public fun verify() {
        // GIVEN
        val otp = createOtp("000000")
        dao.save(otp)

        // WHEN
        val request = VerifyOTPRequest(code = otp.code)
        val response = rest.postForEntity(url(otp.token), request, Any::class.java)

        // THEN
        assertEquals(HttpStatus.OK, response.statusCode)
    }

    @Test
    public fun notFound() {
        // GIVEN
        val otp = createOtp("000000")

        // WHEN
        val request = VerifyOTPRequest(code = otp.code)
        val ex = assertThrows<HttpClientErrorException> {
            rest.postForEntity(url(otp.token), request, Any::class.java)
        }

        // THEN
        assertEquals(HttpStatus.CONFLICT, ex.statusCode)

        val response = ObjectMapper().readValue(ex.responseBodyAsString, ErrorResponse::class.java)
        assertEquals(ErrorURN.OTP_EXPIRED.urn, response.error.code)
    }

    @Test
    public fun expired() {
        // GIVEN
        val otp = createOtp("000000", expires = System.currentTimeMillis() - 1000)
        dao.save(otp)

        // WHEN
        val request = VerifyOTPRequest(code = otp.code)
        val ex = assertThrows<HttpClientErrorException> {
            rest.postForEntity(url(otp.token), request, Any::class.java)
        }

        // THEN
        assertEquals(HttpStatus.CONFLICT, ex.statusCode)

        val response = ObjectMapper().readValue(ex.responseBodyAsString, ErrorResponse::class.java)
        assertEquals(ErrorURN.OTP_EXPIRED.urn, response.error.code)
    }


    @Test
    public fun codeNotValid() {
        // GIVEN
        val otp = createOtp("000000")
        dao.save(otp)

        // WHEN
        val request = VerifyOTPRequest(code = "this-is-an-invalid-code")
        val ex = assertThrows<HttpClientErrorException> {
            rest.postForEntity(url(otp.token), request, Any::class.java)
        }

        // THEN
        assertEquals(HttpStatus.CONFLICT, ex.statusCode)

        val response = ObjectMapper().readValue(ex.responseBodyAsString, ErrorResponse::class.java)
        assertEquals(ErrorURN.OTP_NOT_VALID.urn, response.error.code)
    }

    private fun url(token: String) = "http://localhost:$port/v1/otp/$token"

    private fun createOtp(code: String, expires: Long = System.currentTimeMillis() + 900000) = OtpEntity(
        token = UUID.randomUUID().toString(),
        code = code,
        expires = expires
    )
}
