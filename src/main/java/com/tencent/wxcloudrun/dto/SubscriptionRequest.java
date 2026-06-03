package com.tencent.wxcloudrun.dto;

import lombok.Data;

/**
 * 订阅设置请求
 */
@Data
public class SubscriptionRequest {
    private String regions;       // 逗号分隔：沈阳,大连
    private String categories;    // 逗号分隔：gwy,sydw
    private String keywords;      // 关键词
    private Integer pushEnabled;  // 1开 0关
}
