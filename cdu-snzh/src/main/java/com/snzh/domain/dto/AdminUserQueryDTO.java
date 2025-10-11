package com.snzh.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author haibara
 * @description 管理员查询DTO
 * @since 2025/10/11
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "管理员查询DTO")
public class AdminUserQueryDTO extends BasePageDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户名（模糊搜索）")
    private String username;

    @Schema(description = "真实姓名（模糊搜索）")
    private String realName;

    @Schema(description = "角色类型：0-超级管理员，1-普通管理员")
    private Integer roleType;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;
}

