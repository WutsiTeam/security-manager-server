package com.wutsi.security.endpoint

import com.wutsi.security.`delegate`.CreateUserDelegate
import com.wutsi.security.dto.CreateUserRequest
import com.wutsi.security.dto.CreateUserResponse
import org.springframework.web.bind.`annotation`.PostMapping
import org.springframework.web.bind.`annotation`.RequestBody
import org.springframework.web.bind.`annotation`.RestController
import javax.validation.Valid

@RestController
public class CreateUserController(
    public val `delegate`: CreateUserDelegate
) {
    @PostMapping("/v1/users")
    public fun invoke(
        @Valid @RequestBody
        request: CreateUserRequest
    ): CreateUserResponse =
        delegate.invoke(request)
}
