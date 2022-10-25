package com.wutsi.security.entity


data class OtpEntity(
    val token: String = "",
    val code: String = "",
    val expires: Long = -1
) : java.io.Serializable
