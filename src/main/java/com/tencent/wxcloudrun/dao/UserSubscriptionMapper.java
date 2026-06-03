package com.tencent.wxcloudrun.dao;

import com.tencent.wxcloudrun.model.UserSubscription;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserSubscriptionMapper {

    UserSubscription getByUserId(@Param("userId") Integer userId);

    int insert(UserSubscription subscription);

    int update(UserSubscription subscription);

    /** 查找匹配指定条件的订阅用户ID（用于推送匹配） */
    List<UserSubscription> findMatching(@Param("category") String category,
                                         @Param("region") String region);
}
