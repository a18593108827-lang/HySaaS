package com.hysaas.system.dto;

import lombok.Data;

@Data
public class EnterpriseDashboardStatsVO {
    private long events;
    private long pendingRegistrations;
    private long pendingPapers;
}
