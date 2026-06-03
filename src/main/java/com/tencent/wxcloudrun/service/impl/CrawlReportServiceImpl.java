package com.tencent.wxcloudrun.service.impl;

import com.tencent.wxcloudrun.dao.*;
import com.tencent.wxcloudrun.dto.CrawlReportRequest;
import com.tencent.wxcloudrun.dto.CrawlReportResponse;
import com.tencent.wxcloudrun.model.*;
import com.tencent.wxcloudrun.service.CrawlReportService;
import com.tencent.wxcloudrun.service.PushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class CrawlReportServiceImpl implements CrawlReportService {

    final AnnouncementMapper announcementMapper;
    final AttachmentMapper attachmentMapper;
    final CrawlLogMapper crawlLogMapper;
    final DataSourceMapper dataSourceMapper;
    final PushService pushService;
    final Logger logger = LoggerFactory.getLogger(CrawlReportServiceImpl.class);

    public CrawlReportServiceImpl(@Autowired AnnouncementMapper announcementMapper,
                                   @Autowired AttachmentMapper attachmentMapper,
                                   @Autowired CrawlLogMapper crawlLogMapper,
                                   @Autowired DataSourceMapper dataSourceMapper,
                                   @Autowired PushService pushService) {
        this.announcementMapper = announcementMapper;
        this.attachmentMapper = attachmentMapper;
        this.crawlLogMapper = crawlLogMapper;
        this.dataSourceMapper = dataSourceMapper;
        this.pushService = pushService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CrawlReportResponse receiveReport(CrawlReportRequest request) {
        long startTime = System.currentTimeMillis();
        int received = 0, created = 0, skipped = 0, updated = 0;
        List<Announcement> newAnnouncements = new ArrayList<>();

        try {
            if (request.getItems() == null || request.getItems().isEmpty()) {
                saveCrawlLog(request.getSourceId(), "success", 0, 0, 0, null,
                        System.currentTimeMillis() - startTime);
                return new CrawlReportResponse(0, 0, 0, 0, null);
            }

            received = request.getItems().size();

            for (CrawlReportRequest.CrawlItem item : request.getItems()) {
                try {
                    // 1. 指纹去重
                    String fingerprint = item.getFingerprint();
                    if (fingerprint != null && !fingerprint.isEmpty()) {
                        Announcement existByFP = announcementMapper.getByFingerprint(fingerprint);
                        if (existByFP != null) {
                            skipped++;
                            if (item.getContentText() != null && !item.getContentText().isEmpty()
                                    && (existByFP.getContentText() == null
                                    || !existByFP.getContentText().equals(item.getContentText()))) {
                                existByFP.setContentText(item.getContentText());
                                existByFP.setContentHtml(item.getContentHtml());
                                announcementMapper.update(existByFP);
                                updated++;
                            }
                            continue;
                        }
                    }

                    // 2. URL 降级去重
                    if (item.getOriginUrl() != null) {
                        Announcement existByURL = announcementMapper.getByOriginUrl(item.getOriginUrl());
                        if (existByURL != null) {
                            skipped++;
                            continue;
                        }
                    }

                    // 3. 构建入库
                    Announcement ann = buildAnnouncement(request.getSourceId(), item);
                    announcementMapper.insert(ann);
                    created++;
                    newAnnouncements.add(ann);
                    logger.info("新增公告: id={}, title={}", ann.getId(),
                            ann.getTitle() != null && ann.getTitle().length() > 40
                                    ? ann.getTitle().substring(0, 40) + "..." : ann.getTitle());

                    // 4. 保存附件记录
                    if (item.getAttachments() != null && !item.getAttachments().isEmpty()) {
                        for (CrawlReportRequest.CrawlItem.AttachmentItem att : item.getAttachments()) {
                            Attachment attachment = new Attachment();
                            attachment.setAnnouncementId(ann.getId());
                            attachment.setFileName(att.getFileName());
                            attachment.setFileUrl(att.getFileUrl());
                            attachment.setFileType(att.getFileType());
                            attachment.setFileSize(att.getFileSize());
                            attachment.setParsedStatus(0);
                            attachmentMapper.insert(attachment);
                        }
                        ann.setHasAttachment(1);
                        announcementMapper.update(ann);
                    }

                } catch (Exception e) {
                    logger.error("处理公告条目失败: {} - {}", item.getTitle(), e.getMessage());
                }
            }

            // 5. 更新数据源最后抓取时间
            dataSourceMapper.updateLastCrawlAt(request.getSourceId());

        } catch (Exception e) {
            logger.error("爬虫上报处理异常: {}", e.getMessage());
            saveCrawlLog(request.getSourceId(), "failed", received, created, skipped,
                    e.getMessage(), System.currentTimeMillis() - startTime);
            return new CrawlReportResponse(received, created, skipped, updated, e.getMessage());
        }

        // 6. 记录抓取日志
        saveCrawlLog(request.getSourceId(), "success", received, created, skipped, null,
                System.currentTimeMillis() - startTime);

        // 7. 异步触发推送（后置，不阻塞入库） 
        for (Announcement ann : newAnnouncements) {
            try {
                pushService.matchAndPush(ann);
            } catch (Exception e) {
                logger.warn("推送匹配失败: id={}, error={}", ann.getId(), e.getMessage());
            }
        }

        logger.info("爬虫上报完成: sourceId={}, received={}, created={}, skipped={}, updated={}",
                request.getSourceId(), received, created, skipped, updated);

        return new CrawlReportResponse(received, created, skipped, updated, null);
    }

    private Announcement buildAnnouncement(Integer sourceId, CrawlReportRequest.CrawlItem item) {
        Announcement ann = new Announcement();
        ann.setSourceId(sourceId);
        ann.setOriginUrl(item.getOriginUrl());
        ann.setTitle(item.getTitle());
        ann.setContentHtml(item.getContentHtml());
        ann.setContentText(item.getContentText());
        ann.setCategory(item.getCategory());
        ann.setRegion(item.getRegion());
        ann.setRecruitUnit(item.getRecruitUnit());
        ann.setRecruitCount(item.getRecruitCount());
        ann.setContactPhone(item.getContactPhone());
        ann.setFingerprint(item.getFingerprint());
        ann.setHasAttachment(item.getHasAttachment() != null ? item.getHasAttachment() : 0);
        ann.setStatus(1);
        ann.setViewCount(0);

        try { if (item.getPublishDate() != null) ann.setPublishDate(LocalDate.parse(item.getPublishDate())); }
        catch (Exception e) { ann.setPublishDate(LocalDate.now()); }
        try { if (item.getApplyStart() != null) ann.setApplyStart(LocalDate.parse(item.getApplyStart())); }
        catch (Exception ignored) {}
        try { if (item.getApplyEnd() != null) ann.setApplyEnd(LocalDate.parse(item.getApplyEnd())); }
        catch (Exception ignored) {}
        try { if (item.getExamDate() != null) ann.setExamDate(LocalDate.parse(item.getExamDate())); }
        catch (Exception ignored) {}
        try { if (item.getInterviewDate() != null) ann.setInterviewDate(LocalDate.parse(item.getInterviewDate())); }
        catch (Exception ignored) {}

        return ann;
    }

    private void saveCrawlLog(Integer sourceId, String status, int received,
                               int created, int skipped, String errorMsg, long durationMs) {
        CrawlLog log = new CrawlLog();
        log.setSourceId(sourceId);
        log.setStatus(status);
        log.setTotalUrls(received);
        log.setNewItems(created);
        log.setUpdatedItems(skipped);
        log.setErrorMsg(errorMsg);
        log.setDurationMs(durationMs);
        crawlLogMapper.insert(log);
    }
}
