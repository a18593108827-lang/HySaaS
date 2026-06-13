package com.hysaas.common.validation;

public final class ContactPatterns {

    public static final String EMAIL_REGEX = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";
    public static final String PHONE_REGEX = "^1\\d{10}$";
    public static final String EMAIL_MESSAGE = "请输入有效的邮箱";
    public static final String PHONE_MESSAGE = "请输入有效的手机号";

    private ContactPatterns() {
    }
}
