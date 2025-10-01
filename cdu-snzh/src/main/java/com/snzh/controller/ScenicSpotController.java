package com.snzh.controller;

import cn.hutool.json.JSONObject;
import com.snzh.domain.ResponseResult;
import com.snzh.domain.dto.ScenicQueryDTO;
import com.snzh.domain.dto.ScenicSpotMapRequestDTO;
import com.snzh.domain.dto.ScenicSpotSaveDTO;
import com.snzh.domain.vo.CalculateDistanceMapVO;
import com.snzh.domain.vo.NearByScenicSpotVO;
import com.snzh.domain.vo.PageVo;
import com.snzh.domain.vo.ScenicSpotVO;
import com.snzh.service.IScenicSpotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 景点表 前端控制器
 * </p>
 *
 * @author haibara
 * @since 2025-09-20
 */
@RequestMapping("/scenicSpot")
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "景点API", description = "景点相关接口")
public class ScenicSpotController {

    private final IScenicSpotService scenicSpotService;

    @GetMapping("/detail/{id}")
    @Operation(summary = "获取景点详情", description = "根据ID获取景点详情")
    public ResponseResult<ScenicSpotVO> getScenicSpotDetail(@PathVariable("id") @Parameter(description = "景点ID") Long id) {
        ScenicSpotVO scenicSpot = scenicSpotService.getScenicSpotDetail(id);
        return ResponseResult.success(scenicSpot);
    }

    @GetMapping("/list")
    @Operation(summary = "查询景点列表", description = "查询景点列表")
    public ResponseResult<List<ScenicSpotVO>> getScenicSpotList() {
        List<ScenicSpotVO> result = scenicSpotService.getScenicSpotList();
        return ResponseResult.success(result);
    }

    @GetMapping("/markers")
    @Operation(summary = "获取景区标记点", description = "获取景区标记点信息")
    public ResponseResult<String> getScenicMarkers() {
        String markers = scenicSpotService.getScenicMarkers();
        return ResponseResult.success(markers);
    }

    @PostMapping("/navigation-url")
    @Operation(summary = "生成导航URL", description = "根据ID和导航类型生成高德地图导航URL")
    public ResponseResult<String> getNavigationUrl(@Valid @RequestBody ScenicSpotMapRequestDTO dto) {
        String navigationUrl = scenicSpotService.getNavigationUrl(dto);
        return ResponseResult.success(navigationUrl);
    }

    @PostMapping("/route-planning")
    @Operation(summary = "获取路径规划", description = "获取从用户位置到景点的路径规划信息")
    public ResponseResult<JSONObject> getRoutePlanning(@Valid @RequestBody ScenicSpotMapRequestDTO dto) {
        JSONObject response = scenicSpotService.getRoutePlanning(dto);
        return ResponseResult.success(response);
    }

    @PostMapping("/calculate-distance")
    @Operation(summary = "计算距离", description = "计算用户位置到设施的距离")
    public ResponseResult<CalculateDistanceMapVO> calculateDistance(@Valid @RequestBody ScenicSpotMapRequestDTO dto) {
        CalculateDistanceMapVO response = scenicSpotService.calculateDistance(dto);
        return ResponseResult.success(response);
    }

    @GetMapping("/nearby")
    @Operation(summary = "查询附近景点", description = "根据经纬度查询附近景点")
    public ResponseResult<List<NearByScenicSpotVO>> findNearbySpots(
            @NotNull @RequestParam("longitude") String longitude,
            @NotNull @RequestParam("latitude") String latitude,
            @RequestParam(value = "radius", required = false) Integer radius,
            @NotNull @RequestParam(value = "type") String type,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        List<NearByScenicSpotVO> result = scenicSpotService.findNearbySpots(longitude, latitude, radius, type, page, limit);
        return ResponseResult.success(result);
    }

    @GetMapping("/model-config/{scenicSpotId}")
    @Operation(summary = "获取景点3D模型链接", description = "根据景点id获取景点3D模型链接")
    public ResponseResult<String> getScenicModelConfig(@PathVariable("scenicSpotId") Long scenicSpotId) {
        String model3dUrl = scenicSpotService.getScenicModel3dUrl(scenicSpotId);
        return ResponseResult.success(model3dUrl);
    }

    // -----------------------------管理端-------------------------------------


    @GetMapping("/page")
    @Operation(summary = "分页查询景点列表", description = "分页查询景点列表")
    public ResponseResult<PageVo<ScenicSpotVO>> getScenicPage(@Valid ScenicQueryDTO dto) {
        PageVo<ScenicSpotVO> result = scenicSpotService.getScenicPage(dto);
        return ResponseResult.success(result);
    }

    @PostMapping
    @Operation(summary = "创建景点", description = "创建新的景点")
    public ResponseResult<Long> addScenicSpot(@Valid @RequestBody ScenicSpotSaveDTO dto) {
        Long id = scenicSpotService.addScenicSpot(dto);
        return ResponseResult.success(id);
    }

    @PutMapping("/update")
    @Operation(summary = "修改景点", description = "修改景点信息")
    public ResponseResult<Boolean> updateScenicSpot(@Valid @RequestBody ScenicSpotSaveDTO dto) {
        return ResponseResult.success(scenicSpotService.updateScenicSpot(dto));
    }

    @DeleteMapping("/batch/delete")
    @Operation(summary = "批量删除景点", description = "根据ID批量删除景点")
    public ResponseResult<Boolean> deleteByIds(@RequestBody List<Long> ids) {
        return ResponseResult.success(scenicSpotService.deleteByIds(ids));
    }

    @PutMapping("/{id}/status/{status}")
    @Operation(summary = "更新景点状态", description = "更新景点状态")
    public ResponseResult<Boolean> updateScenicSpotStatus(
            @PathVariable("id") @Parameter(description = "景点ID") Long id,
            @PathVariable("status") @Parameter(description = "状态：0=禁用，1=正常") Integer status) {
        return ResponseResult.success(scenicSpotService.updateScenicSpotStatus(id, status));
    }
}
