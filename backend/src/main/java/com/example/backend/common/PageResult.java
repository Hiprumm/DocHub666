package com.example.backend.common;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果封装。
 *
 * @param <T> 元素类型
 */
@Getter
public class PageResult<T> implements Serializable {

    private final List<T> records;
    private final long total;
    private final int pageNum;
    private final int pageSize;

    private PageResult(List<T> records, long total, int pageNum, int pageSize) {
        this.records = records;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    public static <T> PageResult<T> of(Page<T> page) {
        // JPA Page 页码从 0 开始，这里统一为 1 开始
        return new PageResult<>(page.getContent(), page.getTotalElements(),
                page.getNumber() + 1, page.getSize());
    }
}