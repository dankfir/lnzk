package com.tencent.wxcloudrun.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 分页响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> {
    private java.util.List<T> list;
    private Integer total;
    private Integer page;
    private Integer pageSize;
    private Integer totalPages;

    public PageResult(java.util.List<T> list, Integer total, Integer page, Integer pageSize) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
        this.totalPages = (int) Math.ceil((double) total / pageSize);
    }
}
