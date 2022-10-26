package com.wutsi.security.error

enum class ErrorURN(val urn: String) {
    AUTHENTICATION_MFA_REQUIRED("urn:wutsi:error:security:authentication-mfa-required"),

    KEY_NOT_FOUND("urn:wutsi:error:security:key-not-found"),
    KEY_EXPIRED("urn:wutsi:error:security:key-expired"),

    OTP_ADDRESS_TYPE_NOT_VALID("urn:wutsi:error:security:otp-address-type-not-valid"),
    OTP_EXPIRED("urn:wutsi:error:security:otp-expired"),
    OTP_NOT_VALID("urn:wutsi:error:security:otp-not-valid"),

    PASSWORD_NOT_FOUND("urn:wutsi:error:security:password-not-found"),
    PASSWORD_MISMATCH("urn:wutsi:error:security:password-mismatch")
}
