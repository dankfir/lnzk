package com.tencent.wxcloudrun.dao;

import com.tencent.wxcloudrun.model.CrawlLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CrawlLogMapper {

    int insert(CrawlLog log);

    CrawlLog getLatestBySourceId(Integer sourceId);
}
