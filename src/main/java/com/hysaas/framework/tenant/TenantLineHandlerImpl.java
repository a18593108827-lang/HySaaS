package com.hysaas.framework.tenant;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;

import java.util.Set;

/** MyBatis-Plus 多租户 SQL 改写策略 */
public class TenantLineHandlerImpl implements TenantLineHandler {

    /** 平台级表不做租户过滤 */
    private static final Set<String> IGNORE_TABLES = Set.of(
            "sys_tenant", "sys_config", "sys_user", "pay_transaction"
    );

    @Override
    public Expression getTenantId() {
        Long tenantId = TenantContext.getTenantId();
        return new LongValue(tenantId == null ? 0L : tenantId);
    }

    @Override
    public String getTenantIdColumn() {
        return "tenant_id";
    }

    @Override
    public boolean ignoreTable(String tableName) {
        return IGNORE_TABLES.contains(tableName) || TenantContext.getTenantId() == null;
    }
}
