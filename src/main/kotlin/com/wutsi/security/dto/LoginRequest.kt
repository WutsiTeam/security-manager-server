package com.wutsi.security.dto

import kotlin.Long
import kotlin.String

public data class LoginRequest(
    public val phoneNumber: String = "",
    public val mfaToken: String = "",
    public val accountId: Long = 0,
    public val verificationCode: String = ""
)
