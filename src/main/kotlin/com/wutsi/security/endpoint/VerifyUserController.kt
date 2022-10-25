package com.wutsi.security.endpoint

import com.wutsi.security.`delegate`.VerifyUserDelegate
import com.wutsi.security.dto.VerifyUserRequest
import org.springframework.web.bind.`annotation`.PostMapping
import org.springframework.web.bind.`annotation`.RequestBody
import org.springframework.web.bind.`annotation`.RequestParam
import org.springframework.web.bind.`annotation`.RestController
import javax.validation.Valid
import kotlin.Long

@RestController
public class VerifyUserController(
    public val `delegate`: VerifyUserDelegate
) {
    @PostMapping("/v1/users/{username}/verify")
    public fun invoke(
        @RequestParam(name = "username", required = false) username: Long,
        @Valid
        @RequestBody
        request: VerifyUserRequest
    ) {
        delegate.invoke(username, request)
    }
}
