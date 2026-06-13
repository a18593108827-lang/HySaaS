package com.hysaas.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hysaas.common.exception.BizException;
import com.hysaas.common.validation.ContactPatterns;
import com.hysaas.system.entity.SysUser;
import com.hysaas.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserContactService {

    private final SysUserMapper sysUserMapper;

    public SysUser findByLoginAccount(String account) {
        if (!StringUtils.hasText(account)) {
            return null;
        }
        String value = account.trim();
        SysUser byEmail = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, normalizeEmail(value))
                .last("LIMIT 1"));
        if (byEmail != null) {
            return byEmail;
        }
        if (value.matches(ContactPatterns.PHONE_REGEX)) {
            return sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getPhone, normalizePhone(value))
                    .last("LIMIT 1"));
        }
        return null;
    }

    public void bindContact(SysUser user, String email, String phone, Long excludeUserId) {
        String normalizedEmail = normalizeEmail(email);
        String normalizedPhone = normalizePhone(phone);
        assertEmailFormat(normalizedEmail);
        assertPhoneFormat(normalizedPhone);
        assertEmailAvailable(normalizedEmail, excludeUserId);
        assertPhoneAvailable(normalizedPhone, excludeUserId);
        user.setEmail(normalizedEmail);
        user.setPhone(normalizedPhone);
        user.setUsername(normalizedEmail);
    }

    public String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    public String normalizePhone(String phone) {
        return phone == null ? null : phone.trim();
    }

    private void assertEmailFormat(String email) {
        if (!StringUtils.hasText(email) || !email.matches(ContactPatterns.EMAIL_REGEX)) {
            throw new BizException(ContactPatterns.EMAIL_MESSAGE);
        }
    }

    private void assertPhoneFormat(String phone) {
        if (!StringUtils.hasText(phone) || !phone.matches(ContactPatterns.PHONE_REGEX)) {
            throw new BizException(ContactPatterns.PHONE_MESSAGE);
        }
    }

    private void assertEmailAvailable(String email, Long excludeUserId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, email);
        if (excludeUserId != null) {
            wrapper.ne(SysUser::getId, excludeUserId);
        }
        if (sysUserMapper.selectCount(wrapper) > 0) {
            throw new BizException("邮箱已被使用");
        }
    }

    private void assertPhoneAvailable(String phone, Long excludeUserId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPhone, phone);
        if (excludeUserId != null) {
            wrapper.ne(SysUser::getId, excludeUserId);
        }
        if (sysUserMapper.selectCount(wrapper) > 0) {
            throw new BizException("手机号已被使用");
        }
    }
}
