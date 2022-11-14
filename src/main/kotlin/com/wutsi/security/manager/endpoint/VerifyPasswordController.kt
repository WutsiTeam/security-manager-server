package com.wutsi.security.manager.endpoint

import com.wutsi.security.manager.`delegate`.VerifyPasswordDelegate
import com.wutsi.security.manager.dto.VerifyPasswordRequest
import org.springframework.web.bind.`annotation`.PathVariable
import org.springframework.web.bind.`annotation`.PostMapping
import org.springframework.web.bind.`annotation`.RequestBody
import org.springframework.web.bind.`annotation`.RestController
import javax.validation.Valid
import kotlin.Long

@RestController
public class VerifyPasswordController(
    public val `delegate`: VerifyPasswordDelegate
) {
    @PostMapping("/v1/passwords/{id}/verify")
    public fun invoke(
        @PathVariable(name = "id") id: Long,
        @Valid @RequestBody
        request: VerifyPasswordRequest
    ) {
        delegate.invoke(id, request)
    }
}
