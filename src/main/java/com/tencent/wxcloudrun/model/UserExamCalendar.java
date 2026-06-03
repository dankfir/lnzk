package com.tencent.wxcloudrun.model;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户考试日历
 */
@Data
public class UserExamCalendar implements Serializable {
    private Integer id;
    private Integer userId;
    private Integer announcementId;
    private String eventName;
    private LocalDate eventDate;
    private String eventType;     // apply_start/apply_end/exam/interview/other
    private String note;
    private Integer remindBefore;
    private LocalDateTime createdAt;
}
