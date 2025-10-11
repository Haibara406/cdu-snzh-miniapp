package com.snzh.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.snzh.domain.base.BaseTableData;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author haibara
 * @description 管理员用户实体类
 * @since 2025/10/11
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TableName("admin_user")
@Schema(description = "管理员用户实体类")
@lombok.EqualsAndHashCode(callSuper = false)
public class AdminUser extends BaseTableData implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "管理员ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "登录账号")
    private String username;

    @Schema(description = "登录密码")
    private String password;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "角色类型：0-超级管理员，1-普通管理员")
    private Integer roleType;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;

    @Schema(description = "最后登录IP")
    private String lastLoginIp;
}
