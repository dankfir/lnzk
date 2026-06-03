package com.tencent.wxcloudrun.model;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 抓取日志
 */
@Data
public class CrawlLog implements Serializable {
    private Integer id;
    private Integer sourceId;
    private String status;        // success/partial/failed
    private Integer totalUrls;
    private Integer newItems;
    private Integer updatedItems;
    private String errorMsg;
    private Long durationMs;
    private LocalDateTime createdAt;
}
