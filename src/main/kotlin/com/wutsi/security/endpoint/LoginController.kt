package com.wutsi.security.endpoint

import com.wutsi.security.`delegate`.LoginDelegate
import com.wutsi.security.dto.LoginRequest
import com.wutsi.security.dto.LoginResponse
import org.springframework.web.bind.`annotation`.PostMapping
import org.springframework.web.bind.`annotation`.RequestBody
import org.springframework.web.bind.`annotation`.RestController
import javax.validation.Valid

@RestController
public class LoginController(
    public val `delegate`: LoginDelegate
) {
    @PostMapping("/v1/auth")
    public fun invoke(
        @Valid @RequestBody
        request: LoginRequest
    ): LoginResponse =
        delegate.invoke(request)
}
