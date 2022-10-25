package com.wutsi.security.dto

import kotlin.Long
import kotlin.String

public data class Key(
    public val id: Long = 0,
    public val algorithm: String = "",
    public val content: String = ""
)
