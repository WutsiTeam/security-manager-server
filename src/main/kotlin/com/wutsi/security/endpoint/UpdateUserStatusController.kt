package com.wutsi.security.endpoint

import com.wutsi.security.`delegate`.UpdateUserStatusDelegate
import com.wutsi.security.dto.UpdateUserStatusRequest
import org.springframework.web.bind.`annotation`.PostMapping
import org.springframework.web.bind.`annotation`.RequestBody
import org.springframework.web.bind.`annotation`.RequestParam
import org.springframework.web.bind.`annotation`.RestController
import javax.validation.Valid
import kotlin.Long

@RestController
public class UpdateUserStatusController(
    public val `delegate`: UpdateUserStatusDelegate
) {
    @PostMapping("/v1/users/{id}/status")
    public fun invoke(
        @RequestParam(name = "id", required = false) id: Long,
        @Valid @RequestBody
        request: UpdateUserStatusRequest
    ) {
        delegate.invoke(id, request)
    }
}
