package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.model.Announcement;
import java.util.List;

public interface PushService {

    /**
     * 当有新公告入库时，匹配订阅用户并创建推送记录
     * @param announcement 新入库的公告
     */
    void matchAndPush(Announcement announcement);

    /**
     * 批量匹配推送（批量入库后调用）
     */
    void matchAndPushBatch(List<Announcement> announcements);

    /**
     * 重试失败的推送
     */
    void retryFailed(int limit);
}
