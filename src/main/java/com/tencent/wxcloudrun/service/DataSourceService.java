package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.model.DataSource;

import java.util.List;

public interface DataSourceService {

    List<DataSource> listAll();

    List<DataSource> listEnabled();

    DataSource getById(Integer id);

    DataSource create(DataSource dataSource);

    void update(DataSource dataSource);

    void updateLastCrawlAt(Integer id);

    void updateStatus(Integer id, Integer status);
}
