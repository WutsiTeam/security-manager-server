package com.wutsi.security.dto

import javax.validation.constraints.NotBlank
import kotlin.String

public data class UpdateUserStatusRequest(
    @get:NotBlank
    public val status: String = ""
)
