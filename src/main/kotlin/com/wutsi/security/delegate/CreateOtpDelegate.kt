package com.wutsi.security.`delegate`

import com.wutsi.platform.core.logging.KVLogger
import com.wutsi.security.dto.CreateOTPRequest
import com.wutsi.security.dto.CreateOTPResponse
import com.wutsi.security.service.OtpService
import org.springframework.stereotype.Service

@Service
public class CreateOtpDelegate(
    private val service: OtpService,
    private val logger: KVLogger
) {
    public fun invoke(request: CreateOTPRequest): CreateOTPResponse {
        logger.add("request_address", request.address)
        logger.add("request_type", request.type)

        val otp = service.create(request)
        val messageId = service.send(request, otp)

        logger.add("token", otp.token)
        logger.add("message_id", messageId)
        return CreateOTPResponse(
            token = otp.token
        )
    }
}
