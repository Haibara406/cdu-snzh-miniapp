package com.snzh.controller;

import com.snzh.domain.ResponseResult;
import com.snzh.domain.dto.TicketTypeQueryDTO;
import com.snzh.domain.dto.TicketTypeSaveDTO;
import com.snzh.domain.vo.PageVo;
import com.snzh.domain.vo.TicketTypeVO;
import com.snzh.service.ITicketTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * <p>
 * 票种表 前端控制器
 * </p>
 *
 * @author haibara
 * @since 2025-09-20
 */
@RequestMapping("/ticketType")
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "景点门票API", description = "景点门票相关接口")
public class TicketTypeController {

    private final ITicketTypeService ticketTypeService;

    // -----------------------------管理端-------------------------------------

    @GetMapping("/detail/{id}")
    @Operation(summary = "获取票种详情", description = "根据票种ID获取票种信息")
    public ResponseResult<TicketTypeVO> getTicketTypeDetail(
            @PathVariable("id") @Parameter(description = "票种ID") Long id) {
        TicketTypeVO ticketType = ticketTypeService.getTicketTypeDetail(id);
        return ResponseResult.success(ticketType);
    }

    @GetMapping("/list")
    @Operation(summary = "查询票种列表", description = "获取所有可用票种（不分页）")
    public ResponseResult<List<TicketTypeVO>> listTicketTypes() {
        List<TicketTypeVO> ticketTypes = ticketTypeService.listTicketTypes();
        return ResponseResult.success(ticketTypes);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询票种列表", description = "支持按名称、状态等条件筛选")
    public ResponseResult<PageVo<TicketTypeVO>> getTicketTypePage(@Valid TicketTypeQueryDTO queryDTO) {
        PageVo<TicketTypeVO> page = ticketTypeService.getTicketTypePage(queryDTO);
        return ResponseResult.success(page);
    }

    @PostMapping("/add")
    @Operation(summary = "新增票种", description = "添加新的票种")
    public ResponseResult<Long> addTicketType(@Valid @RequestBody TicketTypeSaveDTO saveDTO) {
        Long id = ticketTypeService.addTicketType(saveDTO);
        return ResponseResult.success(id);
    }

    @PutMapping("/update/{id}")
    @Operation(summary = "修改票种", description = "更新票種信息")
    public ResponseResult<Boolean> updateTicketType(
            @Valid @RequestBody TicketTypeSaveDTO saveDTO) {
        Boolean success = ticketTypeService.updateTicketType(saveDTO);
        return ResponseResult.success(success);
    }

    @DeleteMapping("/batch/delete")
    @Operation(summary = "批量删除票种", description = "根据ID批量删除票种")
    public ResponseResult<Boolean> deleteByIds(@RequestBody List<Long> ids) {
        boolean success = ticketTypeService.deleteByIds(ids);
        return ResponseResult.success(success);
    }

    @PutMapping("/{id}/status/{status}")
    @Operation(summary = "更新票种状态", description = "启用或禁用票种")
    public ResponseResult<Boolean> updateTicketTypeStatus(
            @PathVariable("id") @Parameter(description = "票种ID") Long id,
            @PathVariable("status") @Parameter(description = "状态：0=禁用，1=启用") Integer status) {
        Boolean success = ticketTypeService.updateTicketTypeStatus(id, status);
        return ResponseResult.success(success);
    }

}
