package com.snzh.controller;

import com.snzh.domain.ResponseResult;
import com.snzh.domain.dto.TicketQueryDTO;
import com.snzh.domain.dto.TicketSaveDTO;
import com.snzh.domain.vo.PageVo;
import com.snzh.domain.vo.ScenicTicketVO;
import com.snzh.service.IScenicTicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 景点票种关联表 前端控制器
 * </p>
 *
 * @author haibara
 * @since 2025-09-20
 */
@RequestMapping("/scenicTicket")
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "景点门票API", description = "景点门票相关接口")
public class ScenicTicketController {


    private final IScenicTicketService scenicTicketService;

    @GetMapping("/scenic/{scenicId}")
    @Operation(summary = "获取景点门票信息", description = "根据景点ID获取门票信息")
    public ResponseResult<List<ScenicTicketVO>> getScenicTickets(
            @PathVariable("scenicId") @Parameter(description = "景点ID") Long scenicId) {
        List<ScenicTicketVO> tickets = scenicTicketService.findTicketsByScenicId(scenicId);
        return ResponseResult.success(tickets);
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "根据ID获取门票详情", description = "根据景点票种关联ID获取门票详情")
    public ResponseResult<ScenicTicketVO> getScenicTicketDetail(
            @PathVariable("id") @Parameter(description = "景点票种关联ID") Long id) {
        ScenicTicketVO ticket = scenicTicketService.getScenicTicketDetail(id);
        return ResponseResult.success(ticket);
    }

    // -----------------------------管理端-------------------------------------

    @GetMapping("/page")
    @Operation(summary = "分页查询景点门票列表", description = "支持按名称、类型等条件筛选")
    public ResponseResult<PageVo<ScenicTicketVO>> getTicketPage(@Valid TicketQueryDTO queryDTO) {
        PageVo<ScenicTicketVO> page = scenicTicketService.getTicketPage(queryDTO);
        return ResponseResult.success(page);
    }

    @PostMapping("/add")
    @Operation(summary = "新增门票", description = "添加新的景点门票信息")
    public ResponseResult<Long> addTicket(@Valid @RequestBody TicketSaveDTO saveDTO) {
        Long id = scenicTicketService.addTicket(saveDTO);
        return ResponseResult.success(id);
    }

    @PutMapping("/update/{id}")
    @Operation(summary = "修改门票", description = "更新门票信息")
    public ResponseResult<Boolean> updateTicket(@Valid @RequestBody TicketSaveDTO saveDTO) {
        Boolean success = scenicTicketService.updateTicket(saveDTO);
        return ResponseResult.success(success);
    }

    @DeleteMapping("/batch/delete")
    @Operation(summary = "批量删除门票", description = "批量删除门票")
    public ResponseResult<Boolean> deleteByIds(@RequestBody List<Long> ids) {
        boolean success = scenicTicketService.deleteByIds(ids);
        return ResponseResult.success(success);
    }

    @PutMapping("/{id}/status/{status}")
    @Operation(summary = "更新门票状态", description = "启用或禁用门票")
    public ResponseResult<Boolean> updateTicketStatus(
            @PathVariable("id")  Long id,
            @PathVariable("status") Integer status) {
        Boolean success = scenicTicketService.updateTicketStatus(id, status);
        return ResponseResult.success(success);
    }

}
