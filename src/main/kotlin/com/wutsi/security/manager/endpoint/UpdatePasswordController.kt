package com.wutsi.security.manager.endpoint

import com.wutsi.security.manager.`delegate`.UpdatePasswordDelegate
import com.wutsi.security.manager.dto.UpdatePasswordRequest
import org.springframework.web.bind.`annotation`.PathVariable
import org.springframework.web.bind.`annotation`.PostMapping
import org.springframework.web.bind.`annotation`.RequestBody
import org.springframework.web.bind.`annotation`.RestController
import javax.validation.Valid
import kotlin.Long

@RestController
public class UpdatePasswordController(
    public val `delegate`: UpdatePasswordDelegate
) {
    @PostMapping("/v1/passwords/{id}")
    public fun invoke(
        @PathVariable(name = "id") id: Long,
        @Valid @RequestBody
        request: UpdatePasswordRequest
    ) {
        delegate.invoke(id, request)
    }
}
