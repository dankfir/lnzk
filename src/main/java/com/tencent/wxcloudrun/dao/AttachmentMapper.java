package com.tencent.wxcloudrun.dao;

import com.tencent.wxcloudrun.model.Attachment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AttachmentMapper {

    int insert(Attachment attachment);

    List<Attachment> listByAnnouncementId(@Param("announcementId") Integer announcementId);

    List<Attachment> listUnparsed(@Param("limit") Integer limit);

    int updateParsedStatus(@Param("id") Integer id,
                           @Param("status") Integer status,
                           @Param("parsedData") String parsedData);

    Attachment getById(@Param("id") Integer id);
}
