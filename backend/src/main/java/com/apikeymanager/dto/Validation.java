package com.apikeymanager.dto;

/** Shared regex constants for request validation, used across DTOs. */
public final class Validation {

    /** Letters, digits, colon, underscore, hyphen, dot -- e.g. "read:orders". */
    public static final String SCOPE_PATTERN = "^[A-Za-z0-9_:.-]{1,150}$";

    /**
     * Basic IPv4/IPv6 + optional CIDR suffix shape check (not full RFC
     * validation -- e.g. it won't catch "999.999.999.999"). Good enough to
     * reject obviously-wrong input (HTML, SQL, whitespace) at the API
     * boundary; values are only ever used for string/prefix comparison
     * downstream, never parsed unsafely, so a malformed-but-regex-passing
     * address can't cause anything worse than "this allowlist entry never
     * matches a real request."
     */
    public static final String IP_OR_CIDR_PATTERN =
            "^([0-9a-fA-F:.]{2,45})(/[0-9]{1,3})?$";

    private Validation() {
    }
}
