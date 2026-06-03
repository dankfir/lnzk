package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.dto.CrawlReportRequest;
import com.tencent.wxcloudrun.dto.CrawlReportResponse;
import com.tencent.wxcloudrun.service.CrawlReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 爬虫数据上报接口 — 供 Python 爬虫容器调用
 */
@RestController
@RequestMapping("/api/crawl")
public class CrawlReportController {

    final CrawlReportService crawlReportService;
    final Logger logger = LoggerFactory.getLogger(CrawlReportController.class);

    public CrawlReportController(@Autowired CrawlReportService crawlReportService) {
        this.crawlReportService = crawlReportService;
    }

    /** 接收爬虫上报 */
    @PostMapping("/report")
    ApiResponse report(@RequestBody CrawlReportRequest request) {
        logger.info("收到爬虫上报: sourceId={}, items={}",
                request.getSourceId(),
                request.getItems() != null ? request.getItems().size() : 0);
        CrawlReportResponse resp = crawlReportService.receiveReport(request);
        return ApiResponse.ok(resp);
    }

    /** 心跳检测 — 爬虫可用性检查 */
    @GetMapping("/ping")
    ApiResponse ping() {
        return ApiResponse.ok("crawl-api-ready");
    }
}
