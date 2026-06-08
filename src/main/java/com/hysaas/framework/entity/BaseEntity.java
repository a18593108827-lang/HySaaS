package com.hysaas.framework.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.time.LocalDateTime;

/** 实体基类：雪花 ID、租户、时间戳、逻辑删除 */
@Data
public abstract class BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 多租户隔离字段，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
