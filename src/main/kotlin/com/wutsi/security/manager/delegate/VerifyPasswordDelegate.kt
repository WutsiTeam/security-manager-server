package com.wutsi.security.manager.delegate

import com.wutsi.security.manager.dto.VerifyPasswordRequest
import com.wutsi.security.manager.service.PasswordService
import org.springframework.stereotype.Service

@Service
public class VerifyPasswordDelegate(val service: PasswordService) {
    public fun invoke(request: VerifyPasswordRequest) {
        service.verify(request)
    }
}
