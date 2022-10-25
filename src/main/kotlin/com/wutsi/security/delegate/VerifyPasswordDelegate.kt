package com.wutsi.security.`delegate`

import com.wutsi.security.dto.VerifyPasswordRequest
import com.wutsi.security.service.PasswordService
import org.springframework.stereotype.Service

@Service
public class VerifyPasswordDelegate(val service: PasswordService) {
    public fun invoke(id: Long, request: VerifyPasswordRequest) {
        service.verify(id, request)
    }
}
