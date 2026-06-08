package com.hysaas.framework.tenant;

/** 当前请求的租户 ID，登录后由拦截器写入 */
public final class TenantContext {

    private static final ThreadLocal<Long> TENANT_ID = new ThreadLocal<>();

    public static void setTenantId(Long tenantId) {
        TENANT_ID.set(tenantId);
    }

    public static Long getTenantId() {
        return TENANT_ID.get();
    }

    public static void clear() {
        TENANT_ID.remove();
    }

    private TenantContext() {
    }
}
