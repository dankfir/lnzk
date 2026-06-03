package com.tencent.wxcloudrun.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 爬虫上报响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CrawlReportResponse {
    /** 接收总数 */
    private Integer received;
    /** 新增数 */
    private Integer created;
    /** 跳过数（已存在） */
    private Integer skipped;
    /** 更新数 */
    private Integer updated;
    /** 错误信息 */
    private String error;
}
