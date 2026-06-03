package com.tencent.wxcloudrun.dto;

import lombok.Data;

/**
 * 公告检索请求
 */
@Data
public class AnnouncementSearchRequest {
    private String keyword;
    private String category;    // gwy/sydw/jiaoshi/yiliao/guoqi
    private String region;      // 沈阳/大连/鞍山...
    private String education;   // 大专/本科/研究生...
    private Integer sourceId;
    private Integer page = 1;
    private Integer pageSize = 20;
}
