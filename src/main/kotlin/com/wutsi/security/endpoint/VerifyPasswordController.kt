package com.wutsi.security.endpoint

import com.wutsi.security.`delegate`.VerifyPasswordDelegate
import com.wutsi.security.dto.VerifyPasswordRequest
import org.springframework.web.bind.`annotation`.PostMapping
import org.springframework.web.bind.`annotation`.RequestBody
import org.springframework.web.bind.`annotation`.RequestParam
import org.springframework.web.bind.`annotation`.RestController
import javax.validation.Valid
import kotlin.Long

@RestController
public class VerifyPasswordController(
    public val `delegate`: VerifyPasswordDelegate
) {
    @PostMapping("/v1/passwords/{id}/verify")
    public fun invoke(
        @RequestParam(name = "id", required = false) id: Long,
        @Valid @RequestBody
        request: VerifyPasswordRequest
    ) {
        delegate.invoke(id, request)
    }
}
