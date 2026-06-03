package com.tencent.wxcloudrun.model;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 公告主表
 */
@Data
public class Announcement implements Serializable {
    private Integer id;
    private Integer sourceId;
    private String originUrl;
    private String originId;
    private String title;
    private String contentHtml;
    private String contentText;
    private LocalDate publishDate;
    private String category;      // gwy/sydw/jiaoshi/yiliao/guoqi/qt
    private String region;
    private String recruitUnit;
    private Integer recruitCount;
    private String contactPhone;
    private LocalDate applyStart;
    private LocalDate applyEnd;
    private LocalDate examDate;
    private LocalDate interviewDate;
    private Integer hasAttachment;
    private Integer status;       // 1正常 0下架 2人工修正
    private Integer viewCount;
    private String fingerprint;   // SHA256
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
