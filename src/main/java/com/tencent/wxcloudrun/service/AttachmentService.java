package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.model.Attachment;
import java.util.List;

public interface AttachmentService {

    /** 获取公告的附件列表 */
    List<Attachment> listByAnnouncementId(Integer announcementId);

    /** 获取待解析的附件 */
    List<Attachment> getUnparsed(Integer limit);

    /** 标记附件解析完成 */
    void markParsed(Integer id, String parsedData);

    /** 标记解析失败 */
    void markFailed(Integer id, String errorMsg);
}
