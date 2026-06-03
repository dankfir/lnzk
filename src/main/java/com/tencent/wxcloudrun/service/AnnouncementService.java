package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.dto.AnnouncementSearchRequest;
import com.tencent.wxcloudrun.dto.PageResult;
import com.tencent.wxcloudrun.model.Announcement;

import java.util.List;
import java.util.Optional;

public interface AnnouncementService {

    Optional<Announcement> getById(Integer id);

    Announcement getByIdWithView(Integer id);

    PageResult<Announcement> search(AnnouncementSearchRequest request);

    List<Announcement> listLatest(Integer limit);

    Announcement saveIfNew(Announcement announcement);

    void update(Announcement announcement);

    void updateStatus(Integer id, Integer status);
}
