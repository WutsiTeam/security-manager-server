package com.wutsi.security.endpoint

import com.fasterxml.jackson.databind.ObjectMapper
import com.wutsi.platform.core.error.ErrorResponse
import com.wutsi.security.dao.PasswordRepository
import com.wutsi.security.dto.UpdatePasswordRequest
import com.wutsi.security.error.ErrorURN
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.jdbc.Sql
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(value = ["/db/clean.sql", "/db/UpdatePasswordController.sql"])
class UpdatePasswordControllerTest {
    @LocalServerPort
    val port: Int = 0

    @Autowired
    private lateinit var dao: PasswordRepository

    protected val rest = RestTemplate()

    @Test
    fun update() {
        // WHEN
        val request = UpdatePasswordRequest(
            value = "123"
        )
        val response = rest.postForEntity(url(100), request, Any::class.java)

        // THEN
        assertEquals(HttpStatus.OK, response.statusCode)

        val password = dao.findById(100).get()
        assertEquals(32, password.value.length)
    }

    @Test
    fun notFound() {
        // WHEN
        val request = UpdatePasswordRequest(
            value = "123"
        )
        val ex = assertThrows<HttpClientErrorException> {
            rest.postForEntity(url(999999), request, Any::class.java)
        }

        // THEN
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)

        val response = ObjectMapper().readValue(ex.responseBodyAsString, ErrorResponse::class.java)
        assertEquals(ErrorURN.PASSWORD_NOT_FOUND.urn, response.error.code)
    }

    @Test
    fun deleted() {
        // WHEN
        val request = UpdatePasswordRequest(
            value = "123"
        )
        val ex = assertThrows<HttpClientErrorException> {
            rest.postForEntity(url(999), request, Any::class.java)
        }

        // THEN
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)

        val response = ObjectMapper().readValue(ex.responseBodyAsString, ErrorResponse::class.java)
        assertEquals(ErrorURN.PASSWORD_NOT_FOUND.urn, response.error.code)
    }

    private fun url(id: Long) = "http://localhost:$port/v1/passwords/$id"
}
