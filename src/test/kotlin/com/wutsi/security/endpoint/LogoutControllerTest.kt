package com.wutsi.security.endpoint

import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.wutsi.platform.core.error.ErrorResponse
import com.wutsi.platform.core.security.SubjectType
import com.wutsi.platform.core.security.TokenBlacklistService
import com.wutsi.platform.core.security.spring.jwt.JWTBuilder
import com.wutsi.platform.core.test.TestRSAKeyProvider
import com.wutsi.security.error.ErrorURN
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpRequest
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LogoutControllerTest : ClientHttpRequestInterceptor {
    @LocalServerPort
    public val port: Int = 0

    private lateinit var rest: RestTemplate
    private var token: String? = null

    @MockBean
    private lateinit var tokenBlacklistService: TokenBlacklistService

    @BeforeEach
    fun setUp() {
        rest = RestTemplate()
        rest.interceptors = listOf(this)
    }

    @Test
    fun logout() {
        // GIVEN
        token = createJWTToken(9000)

        // WHEN
        rest.delete(url())

        // THEN
        verify(tokenBlacklistService).add(eq(token!!), any())
    }

    @Test
    fun expired() {
        // GIVEN
        token = createJWTToken(1)

        // WHEN
        Thread.sleep(2000)
        rest.delete(url())

        // THEN
        verify(tokenBlacklistService, never()).add(any(), any())
    }

    @Test
    fun noHeader() {
        // GIVEN
        token = null

        // WHEN
        Thread.sleep(2000)
        val ex = assertThrows<HttpClientErrorException> {
            rest.delete(url())
        }

        // THEN
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
        val response = ObjectMapper().readValue(ex.responseBodyAsString, ErrorResponse::class.java)
        assertEquals(ErrorURN.AUTHORIZATION_HEADER_MISSING.urn, response.error.code)

        verify(tokenBlacklistService, never()).add(any(), any())
    }

    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        if (token != null) {
            request.headers.add("Authorization", "Bearer $token")
        }
        return execution.execute(request, body)
    }

    private fun createJWTToken(ttl: Long) = JWTBuilder(
        subject = "1111",
        subjectType = SubjectType.USER,
        ttl = ttl,
        keyProvider = TestRSAKeyProvider()
    ).build()

    private fun url() = "http://localhost:$port/v1/auth"
}
