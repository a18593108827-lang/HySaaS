package com.hysaas.framework.interceptor;

import cn.dev33.satoken.stp.StpUtil;
import com.hysaas.framework.tenant.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TenantContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (StpUtil.isLogin()) {
            Object tenantId = StpUtil.getSession().get("tenantId");
            if (tenantId instanceof Long id) {
                TenantContext.setTenantId(id);
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        TenantContext.clear();
    }
}
