package com.wutsi.security.`delegate`

import com.wutsi.security.service.PasswordService
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
public class DeletePasswordDelegate(private val service: PasswordService) {
    @Transactional
    public fun invoke(id: Long) {
        service.delete(id)
    }
}
