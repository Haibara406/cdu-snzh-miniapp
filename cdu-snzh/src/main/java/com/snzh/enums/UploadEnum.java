package com.snzh.enums;

import com.snzh.constants.ImageConst;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * @author haibara
 * @description 文件上传枚举
 * @since 2025/7/27 15:47
 */

@Getter
@AllArgsConstructor
@Schema(description = "文件上传类型枚举")
public enum UploadEnum {

    // 景点封面
    @Schema(description = "景点封面")
    ARTICLE_COVER("scenic/scenicCover/", "景点封面", List.of(ImageConst.JPG, ImageConst.JPEG, ImageConst.PNG, ImageConst.WEBP), 5.0, 2),
    // 景点图片
    @Schema(description = "景点图片")
    ARTICLE_IMAGE("scenic/scenicImage/", "景点图片", List.of(ImageConst.JPG, ImageConst.JPEG, ImageConst.PNG, ImageConst.GIF, ImageConst.WEBP), 5.0, 3),
    // 用户头像
    @Schema(description = "用户头像")
    USER_AVATAR("user/avatar/", "用户头像", List.of(ImageConst.JPG, ImageConst.JPEG, ImageConst.PNG, ImageConst.WEBP), 5.0, 4);



    // 上传目录
    private final String dir;

    // 描述
    private final String description;

    // 支持的格式
    private final List<String> format;

    // 文件最大大小 单位：MB
    private final Double limitSize;

    // 上传的类型
    private final int type;
}
