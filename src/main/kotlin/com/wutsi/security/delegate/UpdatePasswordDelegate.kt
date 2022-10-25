package com.wutsi.security.`delegate`

import com.wutsi.security.dto.UpdatePasswordRequest
import com.wutsi.security.service.PasswordService
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
public class UpdatePasswordDelegate(private val service: PasswordService) {
    @Transactional
    public fun invoke(id: Long, request: UpdatePasswordRequest) {
        service.update(id, request)
    }
}
