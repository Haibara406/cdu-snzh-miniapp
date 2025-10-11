package com.snzh.controller;

import com.snzh.annotation.RequireAdmin;
import com.snzh.domain.ResponseResult;
import com.snzh.domain.dto.ScenicImageQueryDTO;
import com.snzh.domain.vo.PageVo;
import com.snzh.domain.vo.ScenicImageVO;
import com.snzh.service.IScenicImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


/**
 * <p>
 * 景点图片表 前端控制器
 * </p>
 *
 * @author haibara
 * @since 2025-09-20
 */
@RequestMapping("/scenicImage")
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "景点图片接口", description = "景点图片相关接口（封面和图集）")
public class ScenicImageController {

    private final IScenicImageService scenicImageService;


    // -----------------------------管理端-------------------------------------

    @RequireAdmin
    @GetMapping("/gallery")
    @Operation(summary = "获取景点图集", description = "根据景点ID获取图片列表")
    public ResponseResult<PageVo<ScenicImageVO>> getImagePage(@Valid ScenicImageQueryDTO dto) {
        PageVo<ScenicImageVO> images = scenicImageService.getImagePage(dto);
        return ResponseResult.success(images);
    }

    @RequireAdmin
    @PostMapping("/upload")
    @Operation(summary = "后台上传照片")
    public ResponseResult<Boolean> uploadPhoto(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") @Length(min = 1, max = 20, message = "照片名称长度为1-20个字符") String name,
            @RequestParam(value = "scenicId") Long scenicId,
            @RequestParam(value = "sort") Integer sort) {
        return ResponseResult.success(scenicImageService.uploadPhoto(file, name, scenicId, sort));
    }

    @RequireAdmin
    @PutMapping("/cover/{scenicId}/{scenicImageId}")
    @Operation(summary = "将图片设置为封面")
    public ResponseResult<Boolean> setCoverPhoto(@PathVariable Integer scenicId, @PathVariable Integer scenicImageId){
        return ResponseResult.success(scenicImageService.setCoverPhoto(scenicId, scenicImageId));
    }

    @RequireAdmin
    @Operation(summary = "后台批量删除照片")
    @DeleteMapping("/batch/delete")
    public ResponseResult<Boolean> deleteByIds(@RequestBody List<Long> ids) {
        return ResponseResult.success(scenicImageService.deleteByIds(ids));
    }


}
