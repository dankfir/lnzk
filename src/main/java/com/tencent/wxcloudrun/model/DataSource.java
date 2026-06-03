package com.tencent.wxcloudrun.model;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据源配置
 */
@Data
public class DataSource implements Serializable {
    private Integer id;
    private String name;
    private String url;
    private String type;
    private String region;
    private String category;
    private String crawlConfig;   // JSON: 抓取规则
    private Integer status;       // 1启用 0停用
    private LocalDateTime lastCrawlAt;
    private Integer crawlInterval;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
