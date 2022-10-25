package com.wutsi.security.`delegate`

import com.wutsi.security.dto.CreatePasswordRequest
import com.wutsi.security.dto.CreatePasswordResponse
import com.wutsi.security.service.PasswordService
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
public class CreatePasswordDelegate(
    private val service: PasswordService
) {
    @Transactional
    public fun invoke(request: CreatePasswordRequest) =
        CreatePasswordResponse(
            passwordId = service.create(request).id!!
        )
}
