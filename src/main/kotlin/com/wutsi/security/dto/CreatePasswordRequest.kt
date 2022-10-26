package com.wutsi.security.dto

import javax.validation.constraints.NotBlank
import kotlin.Long
import kotlin.String

public data class CreatePasswordRequest(
    @get:NotBlank
    public val `value`: String = "",
    public val accountId: Long = 0,
    @get:NotBlank
    public val username: String = ""
)
