package com.wutsi.security.dto

import javax.validation.constraints.NotBlank
import kotlin.String

public data class CreateUserRequest(
    @get:NotBlank
    public val username: String = "",
    @get:NotBlank
    public val password: String = ""
)
