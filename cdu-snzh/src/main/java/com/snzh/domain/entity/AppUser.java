package com.snzh.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serial;
import java.io.Serializable;

import com.snzh.domain.base.BaseTableData;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @author haibara
 * @description 基础设施表
 * @since 2025/9/20 15:34
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TableName("app_user")
@Schema(description = "用户实体类")
public class AppUser extends BaseTableData implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "微信openid")
    private String openid;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "性别（0未知 1男 2女）")
    private Integer gender;

    @Schema(description = "身份证号")
    private String idCard;

    @Schema(description = "真实姓名")
    private String realName;
}
