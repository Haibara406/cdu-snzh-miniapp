package com.snzh.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.snzh.domain.base.BaseTableData;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author haibara
 * @description 消息通知记录实体类
 * @since 2025/10/2
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("notification_record")
@Schema(description = "消息通知记录实体类")
public class NotificationRecord extends BaseTableData implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "通知记录ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户openid")
    private String openid;

    @Schema(description = "通知类型")
    private String notificationType;

    @Schema(description = "模板ID")
    private String templateId;

    @Schema(description = "通知标题")
    private String title;

    @Schema(description = "通知内容JSON")
    private String content;

    @Schema(description = "发送状态：0=发送失败，1=发送成功")
    private Integer sendStatus;

    @Schema(description = "失败原因")
    private String failReason;

    @Schema(description = "关联业务ID（如订单ID）")
    private Long businessId;

    @Schema(description = "发送时间")
    private LocalDateTime sendTime;
}

