package com.tencent.wxcloudrun.service.impl;

import com.tencent.wxcloudrun.dao.AnnouncementMapper;
import com.tencent.wxcloudrun.dto.AnnouncementSearchRequest;
import com.tencent.wxcloudrun.dto.PageResult;
import com.tencent.wxcloudrun.model.Announcement;
import com.tencent.wxcloudrun.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AnnouncementServiceImpl implements AnnouncementService {

    final AnnouncementMapper announcementMapper;

    public AnnouncementServiceImpl(@Autowired AnnouncementMapper announcementMapper) {
        this.announcementMapper = announcementMapper;
    }

    @Override
    public Optional<Announcement> getById(Integer id) {
        return Optional.ofNullable(announcementMapper.getById(id));
    }

    @Override
    public Announcement getByIdWithView(Integer id) {
        Announcement ann = announcementMapper.getById(id);
        if (ann != null) {
            announcementMapper.incrementViewCount(id);
        }
        return ann;
    }

    @Override
    public PageResult<Announcement> search(AnnouncementSearchRequest req) {
        int offset = (req.getPage() - 1) * req.getPageSize();
        List<Announcement> list = announcementMapper.search(
                req.getKeyword(), req.getCategory(), req.getRegion(),
                req.getEducation(), req.getSourceId(), 1, // status=1 只查上架
                offset, req.getPageSize());
        int total = announcementMapper.count(
                req.getKeyword(), req.getCategory(), req.getRegion(), 1);
        return new PageResult<>(list, total, req.getPage(), req.getPageSize());
    }

    @Override
    public List<Announcement> listLatest(Integer limit) {
        return announcementMapper.listLatest(limit);
    }

    @Override
    public Announcement saveIfNew(Announcement announcement) {
        // 指纹去重
        Announcement exist = announcementMapper.getByFingerprint(announcement.getFingerprint());
        if (exist != null) {
            return exist;
        }
        // URL 降级去重
        exist = announcementMapper.getByOriginUrl(announcement.getOriginUrl());
        if (exist != null) {
            return exist;
        }
        announcementMapper.insert(announcement);
        return announcement;
    }

    @Override
    public void update(Announcement announcement) {
        announcementMapper.update(announcement);
    }

    @Override
    public void updateStatus(Integer id, Integer status) {
        announcementMapper.updateStatus(id, status);
    }

    @Override
    public void deleteBySourceId(Integer sourceId) {
        announcementMapper.deleteBySourceId(sourceId);
    }
}
