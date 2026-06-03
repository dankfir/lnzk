package com.tencent.wxcloudrun.model;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 附件记录
 */
@Data
public class Attachment implements Serializable {
    private Integer id;
    private Integer announcementId;
    private String fileName;
    private String fileUrl;
    private String fileType;      // pdf/excel/word/image
    private Long fileSize;
    private Integer parsedStatus; // 0未解析 1已解析 2解析失败
    private String parsedData;    // JSON
    private String ossKey;
    private LocalDateTime createdAt;
}
