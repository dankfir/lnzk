package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.dao.CrawlLogMapper;
import com.tencent.wxcloudrun.model.CrawlLog;
import com.tencent.wxcloudrun.model.DataSource;
import com.tencent.wxcloudrun.service.DataSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据源管理（后台完整CRUD）
 */
@RestController
@RequestMapping("/api/admin/datasource")
public class DataSourceController {

    final DataSourceService dataSourceService;
    final CrawlLogMapper crawlLogMapper;

    public DataSourceController(@Autowired DataSourceService dataSourceService,
                                 @Autowired CrawlLogMapper crawlLogMapper) {
        this.dataSourceService = dataSourceService;
        this.crawlLogMapper = crawlLogMapper;
    }

    /** 获取所有数据源（含停用的） */
    @GetMapping
    ApiResponse list(@RequestParam(defaultValue = "false") Boolean all) {
        List<DataSource> list = all ? dataSourceService.listAll() : dataSourceService.listEnabled();
        return ApiResponse.ok(list);
    }

    @GetMapping("/{id}")
    ApiResponse get(@PathVariable Integer id) {
        DataSource ds = dataSourceService.getById(id);
        if (ds == null) return ApiResponse.error("数据源不存在");
        // 附带最近的抓取日志
        CrawlLog lastLog = crawlLogMapper.getLatestBySourceId(id);
        Map<String, Object> result = new HashMap<>();
        result.put("source", ds);
        result.put("lastCrawlLog", lastLog);
        return ApiResponse.ok(result);
    }

    @PostMapping
    ApiResponse create(@RequestBody DataSource dataSource) {
        if (dataSource.getName() == null || dataSource.getUrl() == null) {
            return ApiResponse.error("名称和URL不能为空");
        }
        return ApiResponse.ok(dataSourceService.create(dataSource));
    }

    @PutMapping("/{id}")
    ApiResponse update(@PathVariable Integer id, @RequestBody DataSource dataSource) {
        dataSource.setId(id);
        dataSourceService.update(dataSource);
        return ApiResponse.ok();
    }

    /** 切换启用/停用 */
    @PutMapping("/{id}/status")
    ApiResponse toggleStatus(@PathVariable Integer id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        if (status == null || (status != 0 && status != 1)) {
            return ApiResponse.error("status 必须为 0 或 1");
        }
        dataSourceService.updateStatus(id, status);
        return ApiResponse.ok();
    }

    /** 测试数据源连通性 */
    @PostMapping("/{id}/test")
    ApiResponse testConnection(@PathVariable Integer id) {
        DataSource ds = dataSourceService.getById(id);
        if (ds == null) return ApiResponse.error("数据源不存在");

        Map<String, Object> result = new HashMap<>();
        result.put("sourceId", id);
        result.put("name", ds.getName());
        long start = System.currentTimeMillis();
        try {
            URL url = new URL(ds.getUrl());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0");
            conn.connect();
            int code = conn.getResponseCode();
            long elapsed = System.currentTimeMillis() - start;
            result.put("reachable", true);
            result.put("httpStatus", code);
            result.put("responseTimeMs", elapsed);
            conn.disconnect();
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            result.put("reachable", false);
            result.put("error", e.getMessage());
            result.put("responseTimeMs", elapsed);
        }
        return ApiResponse.ok(result);
    }

    /** 获取数据源的抓取日志 */
    @GetMapping("/{id}/logs")
    ApiResponse logs(@PathVariable Integer id) {
        CrawlLog lastLog = crawlLogMapper.getLatestBySourceId(id);
        return ApiResponse.ok(lastLog);
    }
}
