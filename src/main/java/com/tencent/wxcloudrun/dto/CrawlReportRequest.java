package com.tencent.wxcloudrun.dto;

import lombok.Data;
import java.util.List;

/**
 * 爬虫上报请求
 */
@Data
public class CrawlReportRequest {
    /** 数据源ID */
    private Integer sourceId;
    /** 公告列表 */
    private List<CrawlItem> items;

    @Data
    public static class CrawlItem {
        private String originUrl;
        private String title;
        private String contentHtml;
        private String contentText;
        private String publishDate;
        private String category;
        private String region;
        private String recruitUnit;
        private Integer recruitCount;
        private String contactPhone;
        private String applyStart;
        private String applyEnd;
        private String examDate;
        private String interviewDate;
        private Integer hasAttachment;
        private String fingerprint;
        /** 附件列表 */
        private List<AttachmentItem> attachments;

        @Data
        public static class AttachmentItem {
            private String fileName;
            private String fileUrl;
            private String fileType;
            private Long fileSize;
        }
    }
}
