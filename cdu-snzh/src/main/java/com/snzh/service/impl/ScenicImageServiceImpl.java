package com.snzh.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.snzh.constants.ErrorConst;
import com.snzh.domain.ResponseResult;
import com.snzh.domain.dto.ScenicImageQueryDTO;
import com.snzh.domain.entity.ScenicImage;
import com.snzh.domain.vo.PageVo;
import com.snzh.domain.vo.ScenicImageVO;
import com.snzh.enums.UploadEnum;
import com.snzh.exceptions.FileUploadException;
import com.snzh.mapper.ScenicImageMapper;
import com.snzh.service.IScenicImageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.snzh.utils.FileUploadUtils;
import com.snzh.utils.PageUtil;
import com.snzh.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


/**
 * <p>
 * 景点图片表 服务实现类
 * </p>
 *
 * @author haibara
 * @since 2025-09-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScenicImageServiceImpl extends ServiceImpl<ScenicImageMapper, ScenicImage> implements IScenicImageService {

    private final ScenicImageMapper scenicImageMapper;
    private final FileUploadUtils fileUploadUtils;


    @Override
    public PageVo<ScenicImageVO> getImagePage(ScenicImageQueryDTO dto) {
        IPage<ScenicImage> page = PageUtil.getPageParams(dto);
        LambdaQueryWrapper<ScenicImage> wrapper = new LambdaQueryWrapper<>();
        if(StringUtils.isNotNull(dto)){
            wrapper.eq(StringUtils.isNotNull(dto.getScenicId()), ScenicImage::getScenicId, dto.getScenicId())
                    .eq(StringUtils.isNotNull(dto.getStatus()), ScenicImage::getStatus, dto.getStatus());
            if(StringUtils.isNotNull(dto.getCreateTimeStart())
                && StringUtils.isNotNull(dto.getCreateTimeEnd())
                && dto.getCreateTimeStart().before(dto.getCreateTimeEnd())){
                wrapper.between(ScenicImage::getCreateTime, dto.getCreateTimeStart(), dto.getCreateTimeEnd());
            }
        }
        wrapper.orderByAsc(ScenicImage::getSize);
        IPage<ScenicImage> scenicImagePage = scenicImageMapper.selectPage(page, wrapper);
        return PageUtil.convertPage(scenicImagePage, scenicImage -> BeanUtil.copyProperties(scenicImage, ScenicImageVO.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean uploadPhoto(MultipartFile file, String name, Long scenicId, Integer sort) {
        try{
            Long count = scenicImageMapper.selectCount(
                    Wrappers.lambdaQuery(ScenicImage.class)
                            .eq(ScenicImage::getName, name)
                            .eq(ScenicImage::getScenicId, scenicId));
            if(count > 0){
                log.error(ErrorConst.PHOTO_NAME_EXIST);
                return false;
            }

            String photoUrl = fileUploadUtils.upload(UploadEnum.SCENIC_IMAGE, file, name);
            scenicImageMapper.insert(ScenicImage.builder()
                    .scenicId(scenicId)
                    .name(name)
                    .imgUrl(photoUrl)
                    .size(fileUploadUtils.convertFileSizeToMB(file.getSize()))
                    .type(UploadEnum.SCENIC_IMAGE.getType())
                    .sort(sort)
                    .build());
            return true;
        }catch (FileUploadException e){
            log.error("{}上传失败", UploadEnum.SCENIC_IMAGE.getDescription(), e);
            return false;
        }catch (Exception e){
            log.error("{}上传出现未知异常", UploadEnum.SCENIC_IMAGE.getDescription(), e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteByIds(List<Long> ids) {
        return removeByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean setCoverPhoto(Integer scenicId, Integer scenicImageId) {
        ScenicImage scenicImage = scenicImageMapper.selectOne(Wrappers.lambdaQuery(ScenicImage.class)
                .eq(ScenicImage::getId, scenicImageId)
                .eq(ScenicImage::getScenicId, scenicId));
        if(StringUtils.isNull(scenicImage)){
            log.error(ErrorConst.PHOTO_NOT_EXIST);
            return false;
        }
        if(scenicImage.getType().equals(UploadEnum.SCENIC_COVER.getType())){
            return true;
        }

        ScenicImage scenicImageCover = scenicImageMapper.selectOne(Wrappers.lambdaQuery(ScenicImage.class)
                .eq(ScenicImage::getScenicId, scenicId)
                .eq(ScenicImage::getType, UploadEnum.SCENIC_COVER.getType()));

        if(StringUtils.isNull(scenicImageCover)){
            scenicImage.setType(UploadEnum.SCENIC_COVER.getType());
            scenicImageMapper.updateById(scenicImage);
            return true;
        }
        scenicImageCover.setType(UploadEnum.SCENIC_IMAGE.getType());
        scenicImage.setType(UploadEnum.SCENIC_COVER.getType());

        scenicImageMapper.updateById(scenicImageCover);
        scenicImageMapper.updateById(scenicImage);
        return true;
    }
}
