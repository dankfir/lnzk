package com.tencent.wxcloudrun.model;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 岗位明细
 */
@Data
public class JobPosition implements Serializable {
    private Integer id;
    private Integer announcementId;
    private String unitName;
    private String positionName;
    private Integer recruitCount;
    private String education;
    private String degree;
    private String major;
    private String ageLimit;
    private String politicalStatus;
    private String workExperience;
    private String otherRequire;
    private String examCategory;
    private String salaryNote;
    private String rawData;
    private LocalDateTime createdAt;
}
