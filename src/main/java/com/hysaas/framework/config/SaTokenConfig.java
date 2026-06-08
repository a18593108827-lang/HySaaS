package com.hysaas.framework.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Sa-Token 登录拦截，白名单路径免鉴权 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/**")
                // 登录、公开接口、支付/发票回调、文档
                .excludePathPatterns(
                        "/auth/login",
                        "/public/**",
                        "/pay/alipay/notify",
                        "/invoice/callback",
                        "/doc.html",
                        "/webjars/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-resources/**",
                        "/health"
                );
    }
}
