package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.dto.AnnouncementSearchRequest;
import com.tencent.wxcloudrun.dto.PageResult;
import com.tencent.wxcloudrun.model.Announcement;
import com.tencent.wxcloudrun.service.AnnouncementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 公告接口
 */
@RestController
@RequestMapping("/api/announcement")
public class AnnouncementController {

    final AnnouncementService announcementService;
    final Logger logger = LoggerFactory.getLogger(AnnouncementController.class);

    public AnnouncementController(@Autowired AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    /** 检索公告 */
    @PostMapping("/search")
    ApiResponse search(@RequestBody AnnouncementSearchRequest request) {
        PageResult<Announcement> result = announcementService.search(request);
        return ApiResponse.ok(result);
    }

    /** 公告详情 */
    @GetMapping("/{id}")
    ApiResponse detail(@PathVariable Integer id) {
        Announcement ann = announcementService.getByIdWithView(id);
        if (ann == null) {
            return ApiResponse.error("公告不存在或已下架");
        }
        return ApiResponse.ok(ann);
    }

    /** 最新公告 */
    @GetMapping("/latest")
    ApiResponse latest(@RequestParam(defaultValue = "10") Integer limit) {
        List<Announcement> list = announcementService.listLatest(limit);
        return ApiResponse.ok(list);
    }

    /** 清空测试数据（管理用） */
    @PostMapping("/admin/clear-test")
    ApiResponse clearTest() {
        // 下架 originUrl 含 test- 的公告
        int count = 0;
        AnnouncementSearchRequest page = new AnnouncementSearchRequest();
        page.setPage(1); page.setPageSize(100);
        PageResult<Announcement> result = announcementService.search(page);
        for (Announcement ann : result.getList()) {
            if (ann.getOriginUrl() != null && ann.getOriginUrl().contains("test-")) {
                announcementService.updateStatus(ann.getId(), 0);
                count++;
            }
        }
        return ApiResponse.ok("已清理 " + count + " 条测试数据");
    }

    /** 首页数据：最新公告 + 按分类统计 */
    @GetMapping("/home")
    ApiResponse home() {
        Map<String, Object> data = new HashMap<>();
        data.put("latest", announcementService.listLatest(20));

        // 按分类取最新5条
        String[] categories = {"gwy", "sydw", "jiaoshi", "yiliao", "guoqi"};
        Map<String, List<Announcement>> byCategory = new HashMap<>();
        for (String cat : categories) {
            AnnouncementSearchRequest req = new AnnouncementSearchRequest();
            req.setCategory(cat);
            req.setPage(1);
            req.setPageSize(5);
            byCategory.put(cat, announcementService.search(req).getList());
        }
        data.put("byCategory", byCategory);
        return ApiResponse.ok(data);
    }
}
