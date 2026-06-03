package com.tencent.wxcloudrun.dao;

import com.tencent.wxcloudrun.model.WxUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WxUserMapper {

    WxUser getByOpenid(@Param("openid") String openid);

    WxUser getById(@Param("id") Integer id);

    int insert(WxUser user);

    int updateLoginTime(@Param("id") Integer id);
}
