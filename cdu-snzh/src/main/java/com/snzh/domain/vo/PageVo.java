package com.snzh.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author haibara
 * @description 分页 VO
 * @since 2025/9/20 10:01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageVo<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private long pageNum;

    private long pageSize;

    private long totalSize;

    private List<T> list;
}
