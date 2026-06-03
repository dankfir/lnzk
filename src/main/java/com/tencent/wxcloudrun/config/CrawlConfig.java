package com.tencent.wxcloudrun.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.crawl")
public class CrawlConfig {
    /** 抓取间隔（分钟） */
    private Integer intervalMinutes = 60;
    /** 是否启用UA轮换 */
    private Boolean userAgentRotation = true;
    /** 请求间隔（毫秒） */
    private Integer requestDelayMs = 3000;
}
