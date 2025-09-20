package com.snzh.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author haibara
 * @description 分页基础dto
 * @since 2025/9/20 10:02
 */
@Data
public class BasePageDto {


    @Schema(name ="pageNumber", type ="Long", description ="页码",requiredMode= Schema.RequiredMode.REQUIRED)
    @NotNull
    private Integer pageNumber;


    @Schema(name ="pageSize", type ="Long", description ="页大小",requiredMode= Schema.RequiredMode.REQUIRED)
    @NotNull
    private Integer pageSize;
}

