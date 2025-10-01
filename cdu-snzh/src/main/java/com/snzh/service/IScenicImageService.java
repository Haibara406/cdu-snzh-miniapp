package com.snzh.service;

import com.snzh.domain.dto.ScenicImageQueryDTO;
import com.snzh.domain.entity.ScenicImage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.snzh.domain.vo.PageVo;
import com.snzh.domain.vo.ScenicImageVO;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


/**
 * <p>
 * 景点图片表 服务类
 * </p>
 *
 * @author haibara
 * @since 2025-09-20
 */
public interface IScenicImageService extends IService<ScenicImage> {

    PageVo<ScenicImageVO> getImagePage(ScenicImageQueryDTO dto);

    Boolean uploadPhoto(
            MultipartFile file,
            @Length(min = 1, max = 20, message = "照片名称长度为1-20个字符") String name,
            Long scenicId,
            Integer sort);


    Boolean deleteByIds(List<Long> ids);

    Boolean setCoverPhoto(Integer scenicId, Integer scenicImageId);
}
