package com.tencent.wxcloudrun.service.impl;

import com.tencent.wxcloudrun.dao.*;
import com.tencent.wxcloudrun.model.*;
import com.tencent.wxcloudrun.service.PushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PushServiceImpl implements PushService {

    final UserSubscriptionMapper subscriptionMapper;
    final WxUserMapper wxUserMapper;
    final PushRecordMapper pushRecordMapper;
    final AnnouncementMapper announcementMapper;
    final Logger logger = LoggerFactory.getLogger(PushServiceImpl.class);
    final RestTemplate restTemplate = new RestTemplate();

    @Value("${wx.miniapp.appid}")
    private String appid;

    @Value("${wx.miniapp.secret}")
    private String secret;

    @Value("${app.push.batch-size:100}")
    private int batchSize;

    @Value("${app.push.max-retry:3}")
    private int maxRetry;

    /** 订阅消息模板ID — 需在微信公众平台申请 */
    private static final String TEMPLATE_ID = "TEMPLATE_ID_PLACEHOLDER";

    private String accessToken;
    private long accessTokenExpireTime;

    public PushServiceImpl(@Autowired UserSubscriptionMapper subscriptionMapper,
                            @Autowired WxUserMapper wxUserMapper,
                            @Autowired PushRecordMapper pushRecordMapper,
                            @Autowired AnnouncementMapper announcementMapper) {
        this.subscriptionMapper = subscriptionMapper;
        this.wxUserMapper = wxUserMapper;
        this.pushRecordMapper = pushRecordMapper;
        this.announcementMapper = announcementMapper;
    }

    @Override
    public void matchAndPush(Announcement announcement) {
        if (announcement == null || announcement.getId() == null) return;

        try {
            // 查找匹配的订阅用户
            List<UserSubscription> matched = subscriptionMapper.findMatching(
                    announcement.getCategory(), announcement.getRegion());

            if (matched.isEmpty()) {
                logger.info("无订阅用户匹配公告: id={}, category={}, region={}",
                        announcement.getId(), announcement.getCategory(), announcement.getRegion());
                return;
            }

            logger.info("公告 id={} 匹配到 {} 个订阅用户", announcement.getId(), matched.size());

            int pushed = 0;
            for (UserSubscription sub : matched) {
                if (pushed >= batchSize) break;

                // 检查是否已推送过
                PushRecord exist = pushRecordMapper.findByUserAndAnn(sub.getUserId(), announcement.getId());
                if (exist != null) continue;

                // 创建推送记录
                PushRecord record = new PushRecord();
                record.setUserId(sub.getUserId());
                record.setAnnouncementId(announcement.getId());
                record.setPushType("subscription");
                record.setStatus("pending");
                record.setPushContent(buildPushContent(announcement));
                pushRecordMapper.insert(record);

                // 实际发送
                boolean sent = sendSubscribeMessage(sub.getUserId(), announcement);
                if (sent) {
                    pushRecordMapper.updateStatus(record.getId(), "success", null);
                } else {
                    pushRecordMapper.updateStatus(record.getId(), "failed", "发送失败");
                }
                pushed++;
            }

            logger.info("公告 id={} 推送完成: {}/{}", announcement.getId(), pushed, matched.size());

        } catch (Exception e) {
            logger.error("推送匹配异常: id={}, error={}", announcement.getId(), e.getMessage());
        }
    }

    @Override
    public void matchAndPushBatch(List<Announcement> announcements) {
        if (announcements == null || announcements.isEmpty()) return;
        for (Announcement ann : announcements) {
            matchAndPush(ann);
        }
    }

    @Override
    public void retryFailed(int limit) {
        List<PushRecord> pending = pushRecordMapper.listPendingRetry(limit);
        for (PushRecord record : pending) {
            Announcement ann = announcementMapper.getById(record.getAnnouncementId());
            if (ann == null) {
                pushRecordMapper.updateStatus(record.getId(), "failed", "公告已删除");
                continue;
            }
            boolean sent = sendSubscribeMessage(record.getUserId(), ann);
            if (sent) {
                pushRecordMapper.updateStatus(record.getId(), "success", null);
            } else {
                pushRecordMapper.updateStatus(record.getId(), "failed", "重试失败");
            }
        }
    }

    // ============ 微信订阅消息发送 ============

    private boolean sendSubscribeMessage(Integer userId, Announcement ann) {
        WxUser user = wxUserMapper.getById(userId);
        if (user == null) {
            logger.warn("用户不存在: userId={}", userId);
            return false;
        }

        String token = getAccessToken();
        if (token == null) return false;

        Map<String, Object> body = new HashMap<>();
        body.put("touser", user.getOpenid());
        body.put("template_id", TEMPLATE_ID);
        body.put("page", "/pages/detail/detail?id=" + ann.getId());

        Map<String, Object> data = new HashMap<>();
        data.put("thing1", mapValue(truncate(ann.getTitle(), 20)));       // 公告标题
        data.put("thing2", mapValue(categoryName(ann.getCategory())));     // 招考类型
        data.put("thing3", mapValue(ann.getRegion() != null ? ann.getRegion() : "辽宁省")); // 地区
        data.put("time4", mapValue(ann.getPublishDate() != null ? ann.getPublishDate().toString() : "")); // 时间
        data.put("thing5", mapValue("点击查看详情")); // 备注
        body.put("data", data);

        try {
            String url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + token;
            Map resp = restTemplate.postForObject(url, body, Map.class);
            if (resp != null && resp.get("errcode") != null && (int) resp.get("errcode") == 0) {
                return true;
            }
            logger.warn("订阅消息发送失败: {}", resp);
            return false;
        } catch (Exception e) {
            logger.error("订阅消息发送异常: {}", e.getMessage());
            return false;
        }
    }

    // ============ Access Token 管理 ============

    private synchronized String getAccessToken() {
        if (accessToken != null && System.currentTimeMillis() < accessTokenExpireTime) {
            return accessToken;
        }
        try {
            String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="
                    + appid + "&secret=" + secret;
            Map resp = restTemplate.getForObject(url, Map.class);
            if (resp != null && resp.get("access_token") != null) {
                accessToken = (String) resp.get("access_token");
                int expiresIn = (int) resp.getOrDefault("expires_in", 7200);
                accessTokenExpireTime = System.currentTimeMillis() + (expiresIn - 300) * 1000L;
                return accessToken;
            }
        } catch (Exception e) {
            logger.error("获取access_token失败: {}", e.getMessage());
        }
        return null;
    }

    // ============ 辅助 ============

    private String buildPushContent(Announcement ann) {
        return String.format("{\"title\":\"%s\",\"category\":\"%s\",\"region\":\"%s\"}",
                ann.getTitle(), ann.getCategory(), ann.getRegion());
    }

    private Map<String, Object> mapValue(String value) {
        Map<String, Object> m = new HashMap<>();
        m.put("value", value != null ? value : "");
        return m;
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() > maxLen ? s.substring(0, maxLen) + "..." : s;
    }

    private String categoryName(String key) {
        if (key == null) return "其他";
        switch (key) {
            case "gwy": return "公务员";
            case "sydw": return "事业单位";
            case "jiaoshi": return "教师招聘";
            case "yiliao": return "医疗卫生";
            case "guoqi": return "国企招聘";
            default: return "其他";
        }
    }
}
