package com.tencent.wxcloudrun.model;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 推送记录
 */
@Data
public class PushRecord implements Serializable {
    private Integer id;
    private Integer userId;
    private Integer announcementId;
    private String pushType;       // subscription / exam_remind
    private String status;         // pending / success / failed
    private String pushContent;    // JSON
    private String errorMsg;
    private Integer retryCount;
    private LocalDateTime createdAt;
}
