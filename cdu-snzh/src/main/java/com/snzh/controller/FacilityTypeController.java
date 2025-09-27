package com.snzh.controller;

import com.snzh.domain.ResponseResult;
import com.snzh.domain.dto.FacilityTypeQueryDTO;
import com.snzh.domain.dto.FacilityTypeSaveDTO;
import com.snzh.domain.vo.FacilityTypeVO;
import com.snzh.domain.vo.PageVo;
import com.snzh.service.IFacilityTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 基础设施类型表 前端控制器
 * </p>
 *
 * @author haibara
 * @since 2025-09-20
 */
@Slf4j
@RestController
@RequestMapping("/facility/types")
@RequiredArgsConstructor
@Tag(name = "设施类型API", description = "设施类型相关接口")
public class FacilityTypeController {

    private final IFacilityTypeService facilityTypeService;

    @GetMapping
    @Operation(summary = "获取所有可用的设施类型", description = "获取所有状态为启用的设施类型")
    public ResponseResult<List<FacilityTypeVO>> getAllEnabledTypes() {
        log.info("获取所有可用的设施类型");
        return ResponseResult.success(facilityTypeService.getAllEnabledTypes());
    }

    // ------------------------- 管理端 ------------------------------

    @GetMapping("/page")
    @Operation(summary = "分页查询设施类型列表", description = "支持按名称等条件筛选")
    public ResponseResult<PageVo<FacilityTypeVO>> getTypePage(@Validated FacilityTypeQueryDTO queryDTO) {
        log.info("分页查询设施类型列表 {}", queryDTO);
        return ResponseResult.success(facilityTypeService.getTypePage(queryDTO));
    }

    @GetMapping("/details/{id}")
    @Operation(summary = "获取设施类型详情", description = "根据ID获取设施类型详情")
    public ResponseResult<FacilityTypeVO> getTypeDetail(@PathVariable("id")  Long id) {
        log.info("获取设施类型详情 : {}", id);
        return ResponseResult.success(facilityTypeService.getTypeById(id));
    }

    @PostMapping
    @Operation(summary = "新增设施类型", description = "添加新的设施类型")
    public ResponseResult<Long> addType(@Validated @RequestBody FacilityTypeSaveDTO saveDTO) {
        log.info("新增设施类型 {}", saveDTO);
        return ResponseResult.success(facilityTypeService.addType(saveDTO));
    }

    @PutMapping("/update/{id}")
    @Operation(summary = "修改设施类型", description = "更新设施类型信息")
    public ResponseResult<Boolean> updateType(@PathVariable("id")  Long id, @Validated @RequestBody FacilityTypeSaveDTO saveDTO) {
        log.info("修改id为{}设施类型 : {}", id, saveDTO);
        return ResponseResult.success(facilityTypeService.updateType(id, saveDTO));
    }

    @DeleteMapping("/batch/delete")
    @Operation(summary = "批量删除设施类型", description = "批量删除设施类型")
    public ResponseResult<Boolean> deleteType(@RequestBody List<Long> ids) {
        log.info("批量删除设施类型 : {}", ids);
        return ResponseResult.success(facilityTypeService.deleteByIds(ids));
    }

    @PutMapping("/{id}/status/{status}")
    @Operation(summary = "更新设施类型状态", description = "启用或禁用设施类型")
    public ResponseResult<Boolean> updateTypeStatus(@PathVariable("id")  Long id, @PathVariable("status")  Integer status) {
        log.info("更新id为 {} 的设施类型状态为 {}", id, status);
        return ResponseResult.success(facilityTypeService.updateTypeStatus(id, status));
    }
}
