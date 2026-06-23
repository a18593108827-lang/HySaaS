package com.hysaas.system.dto;

import lombok.Data;

@Data
public class AdminDashboardStatsVO {
    private long pending;
    private long approved;
    private long total;
}
