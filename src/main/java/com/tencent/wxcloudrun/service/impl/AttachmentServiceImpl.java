package com.tencent.wxcloudrun.service.impl;

import com.tencent.wxcloudrun.dao.AttachmentMapper;
import com.tencent.wxcloudrun.model.Attachment;
import com.tencent.wxcloudrun.service.AttachmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AttachmentServiceImpl implements AttachmentService {

    final AttachmentMapper attachmentMapper;
    final Logger logger = LoggerFactory.getLogger(AttachmentServiceImpl.class);

    public AttachmentServiceImpl(@Autowired AttachmentMapper attachmentMapper) {
        this.attachmentMapper = attachmentMapper;
    }

    @Override
    public List<Attachment> listByAnnouncementId(Integer announcementId) {
        return attachmentMapper.listByAnnouncementId(announcementId);
    }

    @Override
    public List<Attachment> getUnparsed(Integer limit) {
        return attachmentMapper.listUnparsed(limit);
    }

    @Override
    public void markParsed(Integer id, String parsedData) {
        attachmentMapper.updateParsedStatus(id, 1, parsedData);
        logger.info("附件解析完成: id={}", id);
    }

    @Override
    public void markFailed(Integer id, String errorMsg) {
        attachmentMapper.updateParsedStatus(id, 2, errorMsg);
        logger.warn("附件解析失败: id={}, error={}", id, errorMsg);
    }
}
