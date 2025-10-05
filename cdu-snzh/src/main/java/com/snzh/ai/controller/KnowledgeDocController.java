package com.snzh.ai.controller;

import com.snzh.ai.domain.dto.KnowledgeDocQueryDTO;
import com.snzh.ai.domain.dto.KnowledgeDocSaveDTO;
import com.snzh.ai.domain.vo.KnowledgeDocVO;
import com.snzh.ai.service.IKnowledgeDocManageService;
import com.snzh.domain.ResponseResult;
import com.snzh.domain.vo.PageVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author haibara
 * @description AI知识库文档管理控制器
 * @since 2025/10/5
 */
@Slf4j
@RestController
@RequestMapping("/ai/knowledge")
@RequiredArgsConstructor
@Tag(name = "AI知识库管理", description = "AI知识库文档管理接口（管理端）")
public class KnowledgeDocController {

    private final IKnowledgeDocManageService knowledgeDocManageService;

    // -----------------------------管理端-------------------------------------

    @GetMapping("/page")
    @Operation(summary = "分页查询知识库文档", description = "支持按名称、类型、状态等条件筛选")
    public ResponseResult<PageVo<KnowledgeDocVO>> getDocPage(@Valid KnowledgeDocQueryDTO queryDTO) {
        log.info("分页查询知识库文档：{}", queryDTO);
        PageVo<KnowledgeDocVO> page = knowledgeDocManageService.getDocPage(queryDTO);
        return ResponseResult.success(page);
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "获取文档详情", description = "根据ID获取知识库文档详情")
    public ResponseResult<KnowledgeDocVO> getDocDetail(
            @PathVariable("id") @Parameter(description = "文档ID") Long id) {
        log.info("获取文档详情：{}", id);
        KnowledgeDocVO doc = knowledgeDocManageService.getDocDetail(id);
        return ResponseResult.success(doc);
    }

    @GetMapping("/list")
    @Operation(summary = "获取启用的文档列表", description = "获取所有启用状态的文档（不分页）")
    public ResponseResult<List<KnowledgeDocVO>> listEnabledDocs() {
        log.info("获取启用的文档列表");
        List<KnowledgeDocVO> docs = knowledgeDocManageService.listEnabledDocs();
        return ResponseResult.success(docs);
    }

    @PostMapping("/add")
    @Operation(summary = "新增文档", description = "添加新的知识库文档")
    public ResponseResult<Long> addDoc(@Valid @RequestBody KnowledgeDocSaveDTO saveDTO) {
        log.info("新增文档：{}", saveDTO.getDocName());
        Long id = knowledgeDocManageService.addDoc(saveDTO);
        return ResponseResult.success(id);
    }

    @PutMapping("/update")
    @Operation(summary = "修改文档", description = "更新知识库文档信息")
    public ResponseResult<Boolean> updateDoc(@Valid @RequestBody KnowledgeDocSaveDTO saveDTO) {
        log.info("修改文档：{}", saveDTO.getId());
        Boolean success = knowledgeDocManageService.updateDoc(saveDTO);
        return ResponseResult.success(success);
    }

    @DeleteMapping("/batch/delete")
    @Operation(summary = "批量删除文档", description = "批量删除知识库文档（软删除）")
    public ResponseResult<Boolean> deleteByIds(@RequestBody List<Long> ids) {
        log.info("批量删除文档：{}", ids);
        Boolean success = knowledgeDocManageService.deleteByIds(ids);
        return ResponseResult.success(success);
    }

    @PutMapping("/{id}/status/{status}")
    @Operation(summary = "更新文档状态", description = "启用或禁用文档")
    public ResponseResult<Boolean> updateDocStatus(
            @PathVariable("id") @Parameter(description = "文档ID") Long id,
            @PathVariable("status") @Parameter(description = "状态：0=禁用，1=启用") Integer status) {
        log.info("更新文档状态：id={}, status={}", id, status);
        Boolean success = knowledgeDocManageService.updateDocStatus(id, status);
        return ResponseResult.success(success);
    }

    @PostMapping("/rebuild-index")
    @Operation(summary = "重建向量索引", description = "重新加载所有启用的文档并向量化")
    public ResponseResult<Boolean> rebuildVectorIndex() {
        log.info("手动触发重建向量索引");
        Boolean success = knowledgeDocManageService.rebuildVectorIndex();
        return ResponseResult.success(success);
    }
}

