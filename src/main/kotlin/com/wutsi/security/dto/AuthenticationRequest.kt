package com.wutsi.security.dto

import javax.validation.constraints.NotBlank
import kotlin.String

public data class AuthenticationRequest(
    @get:NotBlank
    public val phoneNumber: String = "",
    public val mfaToken: String = "",
    public val verificationCode: String = ""
)
