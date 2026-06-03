package com.tencent.wxcloudrun.dao;

import com.tencent.wxcloudrun.model.DataSource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DataSourceMapper {

    DataSource getById(@Param("id") Integer id);

    List<DataSource> listAll();

    List<DataSource> listEnabled();

    int insert(DataSource dataSource);

    int update(DataSource dataSource);

    int updateLastCrawlAt(@Param("id") Integer id);

    int updateStatus(@Param("id") Integer id, @Param("status") Integer status);
}
