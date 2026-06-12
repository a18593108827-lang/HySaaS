package com.hysaas.framework.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            SaRouter.match("/auth/login", "/public/**", "/health",
                    "/doc.html", "/webjars/**", "/v3/api-docs/**",
                    "/swagger-ui/**", "/swagger-resources/**",
                    "/pay/alipay/notify", "/invoice/callback").stop();
            SaRouter.match("/portal/events/*").matchMethod("GET").stop();
            SaRouter.match("/**").check(r -> StpUtil.checkLogin());
        })).addPathPatterns("/**");
    }
}
