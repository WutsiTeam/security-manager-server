package com.wutsi.security.`delegate`

import com.wutsi.security.dto.VerifyOTPRequest
import com.wutsi.security.service.OtpService
import org.springframework.stereotype.Service

@Service
public class VerifyOtpDelegate(private val service: OtpService) {
    public fun invoke(token: String, request: VerifyOTPRequest) {
        service.verify(token, request)
    }
}
