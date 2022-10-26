package com.wutsi.security.endpoint

import com.wutsi.security.`delegate`.LogoutDelegate
import org.springframework.web.bind.`annotation`.DeleteMapping
import org.springframework.web.bind.`annotation`.RestController

@RestController
public class LogoutController(
    public val `delegate`: LogoutDelegate
) {
    @DeleteMapping("/v1/auth")
    public fun invoke() {
        delegate.invoke()
    }
}
