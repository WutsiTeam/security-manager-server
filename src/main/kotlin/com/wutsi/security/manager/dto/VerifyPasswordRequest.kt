package com.wutsi.security.manager.dto

import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size
import kotlin.String

public data class VerifyPasswordRequest(
    @get:NotBlank
    @get:Size(max = 30)
    public val username: String = "",
    @get:NotBlank
    public val `value`: String = ""
)
