package com.snzh.ai.service;

import com.snzh.ai.domain.dto.KnowledgeDocQueryDTO;
import com.snzh.ai.domain.dto.KnowledgeDocSaveDTO;
import com.snzh.ai.domain.vo.KnowledgeDocVO;
import com.snzh.domain.vo.PageVo;

import java.util.List;

/**
 * @author haibara
 * @description 知识库文档管理服务接口
 * @since 2025/10/5
 */
public interface IKnowledgeDocManageService {

    /**
     * 分页查询知识库文档
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageVo<KnowledgeDocVO> getDocPage(KnowledgeDocQueryDTO queryDTO);

    /**
     * 获取文档详情
     *
     * @param id 文档ID
     * @return 文档详情
     */
    KnowledgeDocVO getDocDetail(Long id);

    /**
     * 获取所有启用的文档列表（不分页）
     *
     * @return 文档列表
     */
    List<KnowledgeDocVO> listEnabledDocs();

    /**
     * 新增文档
     *
     * @param saveDTO 文档信息
     * @return 文档ID
     */
    Long addDoc(KnowledgeDocSaveDTO saveDTO);

    /**
     * 修改文档
     *
     * @param saveDTO 文档信息
     * @return 是否成功
     */
    Boolean updateDoc(KnowledgeDocSaveDTO saveDTO);

    /**
     * 批量删除文档（软删除）
     *
     * @param ids 文档ID列表
     * @return 是否成功
     */
    Boolean deleteByIds(List<Long> ids);

    /**
     * 更新文档状态
     *
     * @param id     文档ID
     * @param status 状态
     * @return 是否成功
     */
    Boolean updateDocStatus(Long id, Integer status);

    /**
     * 重建向量索引（加载所有启用的文档并重新向量化）
     *
     * @return 是否成功
     */
    Boolean rebuildVectorIndex();
}

