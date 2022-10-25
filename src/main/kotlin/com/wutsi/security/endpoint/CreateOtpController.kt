package com.wutsi.security.endpoint

import com.wutsi.security.`delegate`.CreateOtpDelegate
import com.wutsi.security.dto.CreateOTPRequest
import com.wutsi.security.dto.CreateOTPResponse
import org.springframework.web.bind.`annotation`.PostMapping
import org.springframework.web.bind.`annotation`.RequestBody
import org.springframework.web.bind.`annotation`.RestController
import javax.validation.Valid

@RestController
public class CreateOtpController(
    public val `delegate`: CreateOtpDelegate
) {
    @PostMapping("/v1/otp")
    public fun invoke(
        @Valid @RequestBody
        request: CreateOTPRequest
    ): CreateOTPResponse =
        delegate.invoke(request)
}
