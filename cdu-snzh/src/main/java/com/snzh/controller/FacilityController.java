package com.snzh.controller;

import cn.hutool.json.JSONObject;
import com.snzh.domain.ResponseResult;
import com.snzh.domain.dto.FacilityMapRequestDTO;
import com.snzh.domain.dto.FacilityQueryDTO;
import com.snzh.domain.dto.FacilitySaveDTO;
import com.snzh.domain.vo.CalculateDistanceMapVO;
import com.snzh.domain.vo.NearByFacilityVO;
import com.snzh.domain.vo.FacilityVO;
import com.snzh.domain.vo.PageVo;
import com.snzh.service.IFacilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 基础设施表 前端控制器
 * </p>
 *
 * @author haibara
 * @since 2025-09-20
 */
@Slf4j
@RestController
@RequestMapping("/facility")
@RequiredArgsConstructor
@Tag(name = "设施API", description = "设施相关接口")
public class FacilityController {

    private final IFacilityService facilityService;

    @GetMapping("/list")
    @Operation(summary = "根据类型ID获取设施列表", description = "根据设施类型ID获取对应的设施列表")
    public ResponseResult<List<FacilityVO>> getFacilitiesByType(@RequestParam("typeId") Long typeId) {
        List<FacilityVO> facilityList = facilityService.getFacilitiesByType(typeId);
        return ResponseResult.success(facilityList);
    }

    @GetMapping("/details/{id}")
    @Operation(summary = "根据ID获取设施详情", description = "根据设施ID获取设施详细信息")
    public ResponseResult<FacilityVO> getFacilityById(@PathVariable("id") Long id) {
        FacilityVO facilityVO = facilityService.getFacilityById(id);
        return ResponseResult.success(facilityVO);
    }

    @GetMapping("/markers")
    @Operation(summary = "获取设施标记点", description = "根据设施类型ID获取对应的设施标记点信息")
    public ResponseResult<String> getFacilityMarkers(@RequestParam("typeId") Long typeId) {
        String markers = facilityService.getFacilityMarkers(typeId);
        return ResponseResult.success(markers);
    }

    @PostMapping("/navigation-url")
    @Operation(summary = "生成导航URL", description = "根据设施ID和导航类型生成高德地图导航URL")
    public ResponseResult<String> getNavigationUrl(@Validated @RequestBody FacilityMapRequestDTO request) {
        String navigationUrl = facilityService.getNavigationUrl(request);
        return ResponseResult.success(navigationUrl);
    }

    @PostMapping("/route-planning")
    @Operation(summary = "获取路径规划", description = "获取从用户位置到设施的路径规划信息")
    public ResponseResult<JSONObject> getRoutePlanning(@Validated @RequestBody FacilityMapRequestDTO request) {
        JSONObject response = facilityService.getRoutePlanning(request);
        return ResponseResult.success(response);
    }

    @PostMapping("/calculate-distance")
    @Operation(summary = "计算距离", description = "计算用户位置到设施的距离")
    public ResponseResult<CalculateDistanceMapVO> calculateDistance(@Validated @RequestBody FacilityMapRequestDTO request) {
        CalculateDistanceMapVO response = facilityService.calculateDistance(request);
        return ResponseResult.success(response);
    }

    @GetMapping("/nearby")
    @Operation(summary = "查询附近设施", description = "根据用户位置查询附近的设施")
    public ResponseResult<List<NearByFacilityVO>> findNearbyFacilities(
            @NotNull @RequestParam("longitude") String longitude,
            @NotNull @RequestParam("latitude") String latitude,
            @RequestParam(value = "radius", required = false) Integer radius,
            @NotNull @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {

        List<NearByFacilityVO> facilities = facilityService.findNearbyFacilities(
                longitude, latitude, radius, type, page, limit);

        return ResponseResult.success(facilities);
    }

    // -----------------------------管理端-------------------------------------
    @GetMapping("/page")
    @Operation(summary = "分页查询设施列表", description = "支持按名称、类型等条件筛选")
    public ResponseResult<PageVo<FacilityVO>> getFacilityPage(@Validated FacilityQueryDTO queryDTO) {
        PageVo<FacilityVO> page = facilityService.getFacilityPage(queryDTO);
        return ResponseResult.success(page);
    }

    @PostMapping
    @Operation(summary = "新增设施", description = "添加新的设施信息")
    public ResponseResult<Long> addFacility(@Validated @RequestBody FacilitySaveDTO saveDTO) {
        Long id = facilityService.addFacility(saveDTO);
        return ResponseResult.success(id);
    }

    @PutMapping("/update/{id}")
    @Operation(summary = "修改设施", description = "更新设施信息")
    public ResponseResult<Boolean> updateFacility(@PathVariable("id")  Long id, @Validated @RequestBody FacilitySaveDTO saveDTO) {
        Boolean success = facilityService.updateFacility(id, saveDTO);
        return ResponseResult.success(success);
    }

    @DeleteMapping("/batch/delete")
    @Operation(summary = "批量删除设施", description = "批量逻辑删除设施")
    public ResponseResult<Boolean> batchDeleteFacility(@RequestBody List<Long> ids) {
        boolean success = facilityService.deleteByIds(ids);
        return ResponseResult.success(success);
    }

    @PutMapping("/{id}/status/{status}")
    @Operation(summary = "更新设施状态", description = "启用或禁用设施")
    public ResponseResult<Boolean> updateFacilityStatus(@PathVariable("id")  Long id, @PathVariable("status") Integer status) {
        Boolean success = facilityService.updateFacilityStatus(id, status);
        return ResponseResult.success(success);
    }
}
