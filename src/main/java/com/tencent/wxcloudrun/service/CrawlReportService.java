package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.dto.CrawlReportRequest;
import com.tencent.wxcloudrun.dto.CrawlReportResponse;

public interface CrawlReportService {

    /**
     * 接收并处理爬虫上报数据
     */
    CrawlReportResponse receiveReport(CrawlReportRequest request);
}
