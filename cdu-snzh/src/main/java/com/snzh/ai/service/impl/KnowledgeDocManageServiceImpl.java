package com.snzh.ai.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.snzh.ai.domain.dto.KnowledgeDocQueryDTO;
import com.snzh.ai.domain.dto.KnowledgeDocSaveDTO;
import com.snzh.ai.domain.entity.AiKnowledgeDoc;
import com.snzh.ai.domain.vo.KnowledgeDocVO;
import com.snzh.ai.mapper.AiKnowledgeDocMapper;
import com.snzh.ai.service.IKnowledgeBaseService;
import com.snzh.ai.service.IKnowledgeDocManageService;
import com.snzh.domain.vo.PageVo;
import com.snzh.enums.StatusEnum;
import com.snzh.exceptions.DataNotExistException;
import com.snzh.utils.PageUtil;
import com.snzh.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author haibara
 * @description 知识库文档管理服务实现
 * @since 2025/10/5
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeDocManageServiceImpl extends ServiceImpl<AiKnowledgeDocMapper, AiKnowledgeDoc>
        implements IKnowledgeDocManageService {

    private final AiKnowledgeDocMapper knowledgeDocMapper;
    private final IKnowledgeBaseService knowledgeBaseService;

    @Override
    public PageVo<KnowledgeDocVO> getDocPage(KnowledgeDocQueryDTO queryDTO) {
        IPage<AiKnowledgeDoc> page = PageUtil.getPageParams(queryDTO);
        LambdaQueryWrapper<AiKnowledgeDoc> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotNull(queryDTO)) {
            wrapper.like(StringUtils.isNotEmpty(queryDTO.getDocName()),
                            AiKnowledgeDoc::getDocName, queryDTO.getDocName())
                    .eq(StringUtils.isNotEmpty(queryDTO.getDocType()),
                            AiKnowledgeDoc::getDocType, queryDTO.getDocType())
                    .eq(StringUtils.isNotNull(queryDTO.getStatus()),
                            AiKnowledgeDoc::getStatus, queryDTO.getStatus());

            if (StringUtils.isNotNull(queryDTO.getCreateTimeStart())
                    && StringUtils.isNotNull(queryDTO.getCreateTimeEnd())
                    && queryDTO.getCreateTimeStart().before(queryDTO.getCreateTimeEnd())) {
                wrapper.between(AiKnowledgeDoc::getCreateTime,
                        queryDTO.getCreateTimeStart(), queryDTO.getCreateTimeEnd());
            }
        }

        wrapper.orderByDesc(AiKnowledgeDoc::getUpdateTime);
        IPage<AiKnowledgeDoc> docPage = knowledgeDocMapper.selectPage(page, wrapper);

        return PageUtil.convertPage(docPage, this::convertToVO);
    }

    @Override
    public KnowledgeDocVO getDocDetail(Long id) {
        if (StringUtils.isNull(id)) {
            throw new DataNotExistException("文档ID不能为空");
        }

        AiKnowledgeDoc doc = knowledgeDocMapper.selectById(id);
        if (StringUtils.isNull(doc)) {
            throw new DataNotExistException("文档不存在");
        }

        return convertToVO(doc);
    }

    @Override
    public List<KnowledgeDocVO> listEnabledDocs() {
        LambdaQueryWrapper<AiKnowledgeDoc> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiKnowledgeDoc::getStatus, StatusEnum.RUN.getCode())
                .orderByDesc(AiKnowledgeDoc::getUpdateTime);

        List<AiKnowledgeDoc> docs = knowledgeDocMapper.selectList(wrapper);
        return docs.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addDoc(KnowledgeDocSaveDTO saveDTO) {
        log.info("新增知识库文档：{}", saveDTO.getDocName());

        AiKnowledgeDoc doc = AiKnowledgeDoc.builder()
                .docName(saveDTO.getDocName())
                .docType(saveDTO.getDocType())
                .content(saveDTO.getContent())
                .status(saveDTO.getStatus() != null ? saveDTO.getStatus() : StatusEnum.RUN.getCode())
                .build();

        knowledgeDocMapper.insert(doc);

        // 如果是启用状态，添加到向量库
        if (doc.getStatus().equals(StatusEnum.RUN.getCode())) {
            try {
                knowledgeBaseService.addDocument(doc);
                log.info("文档已向量化：{}", doc.getDocName());
            } catch (Exception e) {
                log.error("文档向量化失败：{}", doc.getDocName(), e);
            }
        }

        return doc.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateDoc(KnowledgeDocSaveDTO saveDTO) {
        if (StringUtils.isNull(saveDTO.getId())) {
            throw new DataNotExistException("文档ID不能为空");
        }

        log.info("修改知识库文档：{}", saveDTO.getId());

        AiKnowledgeDoc doc = knowledgeDocMapper.selectById(saveDTO.getId());
        if (StringUtils.isNull(doc)) {
            throw new DataNotExistException("文档不存在");
        }

        // 更新字段
        doc.setDocName(saveDTO.getDocName());
        doc.setDocType(saveDTO.getDocType());
        doc.setContent(saveDTO.getContent());
        if (saveDTO.getStatus() != null) {
            doc.setStatus(saveDTO.getStatus());
        }

        int result = knowledgeDocMapper.updateById(doc);

        // 修改后重建索引（因为内容可能变化）
        if (result > 0) {
            try {
                knowledgeBaseService.rebuildIndex();
                log.info("向量索引已重建");
            } catch (Exception e) {
                log.error("向量索引重建失败", e);
            }
        }

        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }

        log.info("批量删除知识库文档：{}", ids);

        // 软删除：设置status=0
        List<AiKnowledgeDoc> docs = knowledgeDocMapper.selectBatchIds(ids);
        for (AiKnowledgeDoc doc : docs) {
            doc.setStatus(StatusEnum.STOP.getCode());
        }

        boolean result = updateBatchById(docs);

        // 删除后重建索引
        if (result) {
            try {
                knowledgeBaseService.rebuildIndex();
                log.info("向量索引已重建");
            } catch (Exception e) {
                log.error("向量索引重建失败", e);
            }
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateDocStatus(Long id, Integer status) {
        if (StringUtils.isNull(id)) {
            throw new DataNotExistException("文档ID不能为空");
        }

        log.info("更新文档状态：id={}, status={}", id, status);

        AiKnowledgeDoc doc = knowledgeDocMapper.selectById(id);
        if (StringUtils.isNull(doc)) {
            throw new DataNotExistException("文档不存在");
        }

        doc.setStatus(status);
        int result = knowledgeDocMapper.updateById(doc);

        // 状态变更后重建索引
        if (result > 0) {
            try {
                knowledgeBaseService.rebuildIndex();
                log.info("向量索引已重建");
            } catch (Exception e) {
                log.error("向量索引重建失败", e);
            }
        }

        return result > 0;
    }

    @Override
    public Boolean rebuildVectorIndex() {
        try {
            log.info("手动触发向量索引重建");
            knowledgeBaseService.rebuildIndex();
            return true;
        } catch (Exception e) {
            log.error("向量索引重建失败", e);
            return false;
        }
    }

    /**
     * 实体转VO
     */
    private KnowledgeDocVO convertToVO(AiKnowledgeDoc doc) {
        KnowledgeDocVO vo = BeanUtil.copyProperties(doc, KnowledgeDocVO.class);
        if (StringUtils.isNotEmpty(doc.getContent())) {
            vo.setContentLength(doc.getContent().length());
        }
        return vo;
    }
}

