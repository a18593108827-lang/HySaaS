package com.hysaas.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hysaas.system.dto.GlobalConfigVO;
import com.hysaas.system.entity.SysConfig;
import com.hysaas.system.mapper.SysConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ConfigService {

    private static final List<String> CONFIG_KEYS = List.of(
            "smtpHost", "smtpPort", "smtpUser", "smtpPass",
            "alipayAppId", "alipayPrivateKey", "alipayPublicKey", "alipayNotifyUrl", "alipayReturnUrl",
            "wechatMchId", "wechatAppId", "wechatApiV3Key", "wechatPrivateKey", "wechatSerialNo",
            "wechatNotifyUrl", "wechatReturnUrl",
            "invoiceAppKey", "invoiceAppSecret"
    );

    private final SysConfigMapper sysConfigMapper;

    public GlobalConfigVO getGlobalConfig() {
        GlobalConfigVO vo = new GlobalConfigVO();
        Map<String, String> stored = loadAll();
        CONFIG_KEYS.forEach(key -> {
            String value = stored.get(key);
            if (value != null) {
                setProperty(vo, key, value);
            }
        });
        return vo;
    }

    @Transactional
    public GlobalConfigVO updateGlobalConfig(Map<String, String> payload) {
        if (payload == null || payload.isEmpty()) {
            return getGlobalConfig();
        }
        payload.forEach((key, value) -> {
            if (!CONFIG_KEYS.contains(key)) {
                return;
            }
            SysConfig config = sysConfigMapper.selectOne(new LambdaQueryWrapper<SysConfig>()
                    .eq(SysConfig::getConfigKey, key)
                    .last("LIMIT 1"));
            if (config == null) {
                config = new SysConfig();
                config.setConfigKey(key);
                config.setConfigValue(value);
                config.setUpdatedAt(LocalDateTime.now());
                sysConfigMapper.insert(config);
            } else {
                config.setConfigValue(value);
                config.setUpdatedAt(LocalDateTime.now());
                sysConfigMapper.updateById(config);
            }
        });
        return getGlobalConfig();
    }

    public String getValue(String key) {
        SysConfig config = sysConfigMapper.selectOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, key)
                .last("LIMIT 1"));
        return config == null ? null : config.getConfigValue();
    }

    private Map<String, String> loadAll() {
        List<SysConfig> configs = sysConfigMapper.selectList(new LambdaQueryWrapper<SysConfig>()
                .in(SysConfig::getConfigKey, CONFIG_KEYS));
        Map<String, String> map = new HashMap<>();
        for (SysConfig config : configs) {
            map.put(config.getConfigKey(), config.getConfigValue());
        }
        return map;
    }

    private void setProperty(GlobalConfigVO vo, String key, String value) {
        try {
            PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(GlobalConfigVO.class, key);
            if (pd == null || pd.getWriteMethod() == null) {
                return;
            }
            Method writeMethod = pd.getWriteMethod();
            writeMethod.invoke(vo, value);
        } catch (ReflectiveOperationException ignored) {
        }
    }
}
