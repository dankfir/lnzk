package com.tencent.wxcloudrun.dao;

import com.tencent.wxcloudrun.model.Announcement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AnnouncementMapper {

    Announcement getById(@Param("id") Integer id);

    Announcement getByFingerprint(@Param("fingerprint") String fingerprint);

    Announcement getByOriginUrl(@Param("originUrl") String originUrl);

    int insert(Announcement announcement);

    int update(Announcement announcement);

    int updateStatus(@Param("id") Integer id, @Param("status") Integer status);

    int incrementViewCount(@Param("id") Integer id);

    /** 多条件检索（支持全文检索） */
    List<Announcement> search(@Param("keyword") String keyword,
                              @Param("category") String category,
                              @Param("region") String region,
                              @Param("education") String education,
                              @Param("sourceId") Integer sourceId,
                              @Param("status") Integer status,
                              @Param("offset") Integer offset,
                              @Param("limit") Integer limit);

    int count(@Param("keyword") String keyword,
              @Param("category") String category,
              @Param("region") String region,
              @Param("status") Integer status);

    List<Announcement> listLatest(@Param("limit") Integer limit);

    List<Announcement> listBySourceId(@Param("sourceId") Integer sourceId);

    /** 批量下架超过指定天数的公告 */
    int expireOlderThan(@Param("days") Integer days);
}
