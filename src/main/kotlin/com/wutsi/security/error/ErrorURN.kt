package com.wutsi.security.error

enum class ErrorURN(val urn: String) {
    ADDRESS_TYPE_NOT_VALID("urn:wutsi:error:security:address-type-not-valid"),
    OTP_EXPIRED("urn:wutsi:error:security:otp-expired"),
    OTP_NOT_VALID("urn:wutsi:error:security:otp-not-valid")
}
