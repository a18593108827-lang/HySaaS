package com.hysaas.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_tenant")
public class SysTenant {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String name;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private String address;
    private String remark;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
