package com.wutsi.security.endpoint

import com.wutsi.security.dao.PasswordRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.jdbc.Sql
import org.springframework.web.client.RestTemplate
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(value = ["/db/clean.sql", "/db/DeletePasswordController.sql"])
public class DeletePasswordControllerTest {
    @LocalServerPort
    public val port: Int = 0

    @Autowired
    private lateinit var dao: PasswordRepository

    protected val rest = RestTemplate()

    @Test
    fun update() {
        // WHEN
        rest.delete(url(100))

        // THEN
        val password = dao.findById(100).get()
        assertTrue(password.isDeleted)
        assertNotNull(password.deleted)
    }

    @Test
    fun notFound() {
        rest.delete(url(99999))
    }

    @Test
    fun deleted() {
        rest.delete(url(999))

        // THEN
        val password = dao.findById(999).get()
        assertTrue(password.isDeleted)
        assertNotNull(password.deleted)
    }

    private fun url(id: Long) = "http://localhost:$port/v1/passwords/$id"
}
