package com.hysaas.common.validation;

public final class AccountPatterns {

    public static final String REGEX = "^(?:[^\\s@]+@[^\\s@]+\\.[^\\s@]+|1\\d{10})$";
    public static final String MESSAGE = "账号必须是有效邮箱或手机号";

    private AccountPatterns() {
    }
}
