package com.tencent.wxcloudrun.task;

import com.tencent.wxcloudrun.dao.AnnouncementMapper;
import com.tencent.wxcloudrun.model.Attachment;
import com.tencent.wxcloudrun.model.DataSource;
import com.tencent.wxcloudrun.service.DataSourceService;
import com.tencent.wxcloudrun.service.PushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 定时任务调度
 * - attachmentParseTask: 5分钟一次，解析待处理的附件
 * - crawlHealthCheck: 30分钟一次，检查数据源健康
 */
@Component
public class ScheduledTasks {

    final DataSourceService dataSourceService;
    final PushService pushService;
    final AnnouncementMapper announcementMapper;
    final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

    public ScheduledTasks(@Autowired DataSourceService dataSourceService,
                           @Autowired PushService pushService,
                           @Autowired AnnouncementMapper announcementMapper) {
        this.dataSourceService = dataSourceService;
        this.pushService = pushService;
        this.announcementMapper = announcementMapper;
    }

    /**
     * 数据源健康检查（每30分钟）
     * 实际抓取在 Python 爬虫容器中执行，这里只做状态监控
     */
    @Scheduled(fixedRate = 30 * 60 * 1000)
    public void crawlHealthCheck() {
        logger.info("[定时任务] 数据源健康检查");
        List<DataSource> sources = dataSourceService.listEnabled();
        for (DataSource ds : sources) {
            long sinceLastCrawl = 0;
            if (ds.getLastCrawlAt() != null) {
                sinceLastCrawl = java.time.Duration.between(
                        ds.getLastCrawlAt(), java.time.LocalDateTime.now()).toMinutes();
            }
            if (sinceLastCrawl > 120) {
                logger.warn("[健康检查] 数据源 {} 超过2小时未抓取，最后抓取: {}",
                        ds.getName(), ds.getLastCrawlAt());
            }
        }
        logger.info("[定时任务] 健康检查完成，活跃数据源: {}", sources.size());
    }

    /**
     * 公告过期标记（每天凌晨3点）
     * 将 publish_date 超过 90 天的公告标记为下架
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void expireAnnouncements() {
        logger.info("[定时任务] 公告过期检查");
        try {
            int expired = announcementMapper.expireOlderThan(90);
            logger.info("[定时任务] 下架过期公告: {} 条", expired);
        } catch (Exception e) {
            logger.error("[定时任务] 过期公告处理异常: {}", e.getMessage());
        }
    }

    /**
     * 重试失败推送（每10分钟）
     */
    @Scheduled(fixedRate = 10 * 60 * 1000)
    public void retryFailedPush() {
        logger.info("[定时任务] 重试失败推送");
        try {
            pushService.retryFailed(50);
        } catch (Exception e) {
            logger.error("重试推送异常: {}", e.getMessage());
        }
    }
}
