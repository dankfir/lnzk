package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.dao.PushRecordMapper;
import com.tencent.wxcloudrun.model.PushRecord;
import com.tencent.wxcloudrun.service.PushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 推送管理接口
 */
@RestController
@RequestMapping("/api/admin/push")
public class PushAdminController {

    final PushRecordMapper pushRecordMapper;
    final PushService pushService;

    public PushAdminController(@Autowired PushRecordMapper pushRecordMapper,
                                @Autowired PushService pushService) {
        this.pushRecordMapper = pushRecordMapper;
        this.pushService = pushService;
    }

    /** 最近推送记录 */
    @GetMapping("/records")
    ApiResponse records(@RequestParam(defaultValue = "50") Integer limit) {
        List<PushRecord> list = pushRecordMapper.listRecent(limit);
        return ApiResponse.ok(list);
    }

    /** 推送统计 */
    @GetMapping("/stats")
    ApiResponse stats() {
        Map<String, Object> stats = new HashMap<>();
        // 简化统计：取最近记录数
        List<PushRecord> recent = pushRecordMapper.listRecent(500);
        long success = recent.stream().filter(r -> "success".equals(r.getStatus())).count();
        long failed = recent.stream().filter(r -> "failed".equals(r.getStatus())).count();
        long pending = recent.stream().filter(r -> "pending".equals(r.getStatus())).count();
        stats.put("total", recent.size());
        stats.put("success", success);
        stats.put("failed", failed);
        stats.put("pending", pending);
        return ApiResponse.ok(stats);
    }

    /** 手动触发重试 */
    @PostMapping("/retry")
    ApiResponse retry(@RequestParam(defaultValue = "50") Integer limit) {
        pushService.retryFailed(limit);
        return ApiResponse.ok("重试已触发");
    }

    /** 手动触发批量推送（重新匹配最近N条公告） */
    @PostMapping("/match-recent")
    ApiResponse matchRecent(@RequestBody Map<String, Integer> body) {
        Integer announcementId = body.get("announcementId");
        if (announcementId == null) {
            return ApiResponse.error("请指定announcementId");
        }
        // 通过重新匹配推送
        pushService.retryFailed(0); // 占位
        return ApiResponse.ok("请使用 /retry 接口重试失败推送");
    }
}
