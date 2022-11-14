package com.wutsi.security.manager.dto

import kotlin.String

public data class LoginRequest(
    public val phoneNumber: String = "",
    public val mfaToken: String = "",
    public val verificationCode: String = ""
)
