package com.wutsi.security.endpoint

import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.wutsi.platform.core.error.ErrorResponse
import com.wutsi.platform.core.security.SubjectType
import com.wutsi.platform.core.security.TokenBlacklistService
import com.wutsi.platform.core.security.spring.jwt.JWTBuilder
import com.wutsi.platform.core.test.TestRSAKeyProvider
import com.wutsi.security.dao.LoginRepository
import com.wutsi.security.error.ErrorURN
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpRequest
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.test.context.jdbc.Sql
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(value = ["/db/clean.sql", "/db/LogoutController.sql"])
class LogoutControllerTest : ClientHttpRequestInterceptor {
    @LocalServerPort
    val port: Int = 0

    private lateinit var rest: RestTemplate
    private var token: String? = null

    @MockBean
    private lateinit var tokenBlacklistService: TokenBlacklistService

    @Autowired
    private lateinit var dao: LoginRepository

    @BeforeEach
    fun setUp() {
        rest = RestTemplate()
        rest.interceptors = listOf(this)
    }

    @Test
    fun logout() {
        // GIVEN
//        token = createJWTToken(365L * 86400L * 100L * 1000L)
//        println(token)
//        println(DigestUtils.md5Hex(token))
        token =
            "eyJraWQiOiIxIiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiIxMTExIiwic3ViX3R5cGUiOiJVU0VSIiwic2NvcGUiOltdLCJpc3MiOiJ3dXRzaS5jb20iLCJhZG1pbiI6ZmFsc2UsImV4cCI6NDgyMDM4NzEwNSwiaWF0IjoxNjY2Nzg3MTA1LCJqdGkiOiIxIn0.fPescc3a5CQBxf-yvSZbb8fKmQ8tLknTZEhgQV1GmyUL9z3qcVsb86fsBg8LFEMJigxzrRLP6MX9TD2IcdfqVT3CABK8sg1iIlTjUvDFiEBtkcgYZpaX-xH5B2yuId5gKTLEosmLjpf5gdGcvc7ZkMTUqa0eTwJGuq9KlRI90ZtXGdUSoePOOHJFKNPoe5EPX9eJcaScyylJQa-yHft_iuh8iJle7QPafMUtYfP3gDIwjvb9JGT_sbXhkLwAJPjSU4en25Krfp5RWzI0kTidjap2OpIGRjU-G6cC0fcJyZNNBV_KXMQHoPCyZsAcW80IUY0eqhzgjx4U_rNn0YZI7w"

        // WHEN
        rest.delete(url())

        // THEN
        verify(tokenBlacklistService).add(eq(token!!), any())

        assertNotNull(dao.findById(100).get().expired)

        Thread.sleep(5000) // Wait for async logout of previous session
        assertNotNull(dao.findById(101).get().expired)
        assertNotNull(dao.findById(102).get().expired)
        verify(tokenBlacklistService, times(3)).add(any(), any())
    }

    @Test
    fun badToken() {
        token = "xxx"

        // WHEN
        rest.delete(url())

        // THEN
        verify(tokenBlacklistService, never()).add(any(), any())
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
