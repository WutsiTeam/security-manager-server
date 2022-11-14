package com.wutsi.security.manager.delegate

import com.wutsi.security.manager.dto.VerifyOTPRequest
import com.wutsi.security.manager.service.OtpService
import org.springframework.stereotype.Service

@Service
public class VerifyOtpDelegate(private val service: OtpService) {
    public fun invoke(token: String, request: VerifyOTPRequest) {
        service.verify(token, request)
    }
}
