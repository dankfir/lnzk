package com.tencent.wxcloudrun.dao;

import com.tencent.wxcloudrun.model.PushRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PushRecordMapper {

    int insert(PushRecord record);

    int updateStatus(@Param("id") Integer id,
                     @Param("status") String status,
                     @Param("errorMsg") String errorMsg);

    /** 查询用户是否已推送过该公告 */
    PushRecord findByUserAndAnn(@Param("userId") Integer userId,
                                 @Param("announcementId") Integer announcementId);

    /** 待重试的推送 */
    List<PushRecord> listPendingRetry(@Param("limit") Integer limit);

    /** 最近推送记录 */
    List<PushRecord> listRecent(@Param("limit") Integer limit);
}
