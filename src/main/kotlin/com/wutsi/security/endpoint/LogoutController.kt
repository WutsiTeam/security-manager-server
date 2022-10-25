package com.wutsi.security.endpoint

import com.wutsi.security.`delegate`.LogoutDelegate
import org.springframework.web.bind.`annotation`.PostMapping
import org.springframework.web.bind.`annotation`.RequestBody
import org.springframework.web.bind.`annotation`.RestController
import javax.validation.Valid
import kotlin.Any

@RestController
public class LogoutController(
    public val `delegate`: LogoutDelegate
) {
    @PostMapping("/v1/logout")
    public fun invoke(
        @Valid @RequestBody
        request: Any
    ) {
        delegate.invoke(request)
    }
}
