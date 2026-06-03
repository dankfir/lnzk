package com.tencent.wxcloudrun.dao;

import com.tencent.wxcloudrun.model.JobPosition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface JobPositionMapper {

    List<JobPosition> listByAnnouncementId(@Param("announcementId") Integer announcementId);

    int batchInsert(@Param("list") List<JobPosition> list);

    int deleteByAnnouncementId(@Param("announcementId") Integer announcementId);
}
