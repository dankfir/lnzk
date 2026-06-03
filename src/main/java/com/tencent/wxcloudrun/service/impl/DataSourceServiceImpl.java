package com.tencent.wxcloudrun.service.impl;

import com.tencent.wxcloudrun.dao.DataSourceMapper;
import com.tencent.wxcloudrun.model.DataSource;
import com.tencent.wxcloudrun.service.DataSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataSourceServiceImpl implements DataSourceService {

    final DataSourceMapper dataSourceMapper;

    public DataSourceServiceImpl(@Autowired DataSourceMapper dataSourceMapper) {
        this.dataSourceMapper = dataSourceMapper;
    }

    @Override
    public List<DataSource> listEnabled() {
        return dataSourceMapper.listEnabled();
    }

    @Override
    public DataSource getById(Integer id) {
        return dataSourceMapper.getById(id);
    }

    @Override
    public DataSource create(DataSource dataSource) {
        dataSource.setStatus(1);
        dataSourceMapper.insert(dataSource);
        return dataSource;
    }

    @Override
    public void update(DataSource dataSource) {
        dataSourceMapper.update(dataSource);
    }

    @Override
    public void updateLastCrawlAt(Integer id) {
        dataSourceMapper.updateLastCrawlAt(id);
    }

    @Override
    public void updateStatus(Integer id, Integer status) {
        dataSourceMapper.updateStatus(id, status);
    }

    @Override
    public List<DataSource> listAll() {
        return dataSourceMapper.listAll();
    }
}
