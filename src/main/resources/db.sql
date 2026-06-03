# 辽宁省公职类招考信息聚合 - 微信云托管数据库初始化脚本
# MySQL 5.7

-- ============================================================
-- 1. 数据源配置表
-- ============================================================
CREATE TABLE IF NOT EXISTS `data_source` (
  `id`           INT(11)      NOT NULL AUTO_INCREMENT,
  `name`         VARCHAR(100) NOT NULL COMMENT '数据源名称',
  `url`          VARCHAR(500) NOT NULL COMMENT '入口URL',
  `type`         VARCHAR(30)  NOT NULL DEFAULT 'gov_website' COMMENT '类型：gov_website/rss/api',
  `region`       VARCHAR(50)  DEFAULT NULL COMMENT '地区：沈阳/大连/鞍山...',
  `category`     VARCHAR(50)  DEFAULT NULL COMMENT '分类：gwy/sydw/jiaoshi/yiliao/guoqi',
  `crawl_config` TEXT         COMMENT '抓取规则JSON：列表页CSS选择器、详情页字段提取规则',
  `status`       TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
  `last_crawl_at` DATETIME    DEFAULT NULL COMMENT '最后抓取时间',
  `crawl_interval` INT(11)    DEFAULT 3600 COMMENT '抓取间隔（秒），默认1小时',
  `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_region` (`region`),
  KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据源配置';

-- ============================================================
-- 2. 公告主表
-- ============================================================
CREATE TABLE IF NOT EXISTS `announcement` (
  `id`             INT(11)      NOT NULL AUTO_INCREMENT,
  `source_id`      INT(11)      NOT NULL COMMENT '数据源ID',
  `origin_url`     VARCHAR(500) NOT NULL COMMENT '原始URL',
  `origin_id`      VARCHAR(200) DEFAULT NULL COMMENT '原始站ID（如有）',
  `title`          VARCHAR(500) NOT NULL COMMENT '公告标题',
  `content_html`   MEDIUMTEXT   COMMENT '正文HTML',
  `content_text`   MEDIUMTEXT   COMMENT '正文纯文本',
  `publish_date`   DATE         DEFAULT NULL COMMENT '发布日期',
  `category`       VARCHAR(50)  DEFAULT NULL COMMENT '考试类型：gwy/sydw/jiaoshi/yiliao/guoqi/qt',
  `region`         VARCHAR(50)  DEFAULT NULL COMMENT '地区',
  `recruit_unit`   VARCHAR(200) DEFAULT NULL COMMENT '招聘单位',
  `recruit_count`  INT(11)      DEFAULT NULL COMMENT '招聘人数',
  `contact_phone`  VARCHAR(50)  DEFAULT NULL COMMENT '联系电话',
  `apply_start`    DATE         DEFAULT NULL COMMENT '报名开始',
  `apply_end`      DATE         DEFAULT NULL COMMENT '报名截止',
  `exam_date`      DATE         DEFAULT NULL COMMENT '笔试时间',
  `interview_date` DATE         DEFAULT NULL COMMENT '面试时间',
  `has_attachment` TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否有附件',
  `status`         TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '状态：1正常 0下架 2人工修正',
  `view_count`     INT(11)      NOT NULL DEFAULT 0 COMMENT '浏览量',
  `fingerprint`    VARCHAR(64)  NOT NULL COMMENT '内容指纹SHA256（去重用）',
  `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_fingerprint` (`fingerprint`),
  KEY `idx_source_id` (`source_id`),
  KEY `idx_category` (`category`),
  KEY `idx_region` (`region`),
  KEY `idx_publish_date` (`publish_date`),
  KEY `idx_apply_end` (`apply_end`),
  KEY `idx_status_date` (`status`, `publish_date`),
  FULLTEXT KEY `ft_title_text` (`title`, `content_text`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告主表';

-- ============================================================
-- 3. 岗位明细表
-- ============================================================
CREATE TABLE IF NOT EXISTS `job_position` (
  `id`              INT(11)      NOT NULL AUTO_INCREMENT,
  `announcement_id` INT(11)      NOT NULL COMMENT '公告ID',
  `unit_name`       VARCHAR(200) DEFAULT NULL COMMENT '用人单位',
  `position_name`   VARCHAR(200) DEFAULT NULL COMMENT '岗位名称',
  `recruit_count`   INT(11)      DEFAULT NULL COMMENT '招聘人数',
  `education`       VARCHAR(50)  DEFAULT NULL COMMENT '学历要求',
  `degree`          VARCHAR(50)  DEFAULT NULL COMMENT '学位要求',
  `major`           VARCHAR(500) DEFAULT NULL COMMENT '专业要求',
  `age_limit`       VARCHAR(50)  DEFAULT NULL COMMENT '年龄限制',
  `political_status` VARCHAR(20) DEFAULT NULL COMMENT '政治面貌',
  `work_experience` VARCHAR(100) DEFAULT NULL COMMENT '工作经历要求',
  `other_require`   TEXT         COMMENT '其他条件',
  `exam_category`   VARCHAR(100) DEFAULT NULL COMMENT '考试类别',
  `salary_note`     VARCHAR(200) DEFAULT NULL COMMENT '薪资备注',
  `raw_data`        TEXT         COMMENT '原始行数据JSON',
  `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_announcement_id` (`announcement_id`),
  KEY `idx_education` (`education`),
  KEY `idx_major` (`major`(100)),
  KEY `idx_unit` (`unit_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位明细';

-- ============================================================
-- 4. 附件记录表
-- ============================================================
CREATE TABLE IF NOT EXISTS `attachment` (
  `id`              INT(11)      NOT NULL AUTO_INCREMENT,
  `announcement_id` INT(11)      NOT NULL COMMENT '公告ID',
  `file_name`       VARCHAR(200) NOT NULL COMMENT '文件名',
  `file_url`        VARCHAR(500) NOT NULL COMMENT '原始下载URL',
  `file_type`       VARCHAR(20)  NOT NULL DEFAULT 'pdf' COMMENT '类型：pdf/excel/word/image',
  `file_size`       BIGINT(20)   DEFAULT NULL COMMENT '文件大小（字节）',
  `parsed_status`   TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '解析状态：0未解析 1已解析 2解析失败',
  `parsed_data`     MEDIUMTEXT   COMMENT '解析后结构化数据JSON',
  `oss_key`         VARCHAR(300) DEFAULT NULL COMMENT '对象存储Key',
  `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_announcement_id` (`announcement_id`),
  KEY `idx_parsed_status` (`parsed_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='附件记录';

-- ============================================================
-- 5. 微信用户表
-- ============================================================
CREATE TABLE IF NOT EXISTS `wx_user` (
  `id`          INT(11)     NOT NULL AUTO_INCREMENT,
  `openid`      VARCHAR(64) NOT NULL COMMENT '微信openid',
  `unionid`     VARCHAR(64) DEFAULT NULL COMMENT '微信unionid',
  `nickname`    VARCHAR(100) DEFAULT NULL COMMENT '昵称',
  `avatar_url`  VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
  `last_login_at` DATETIME  DEFAULT NULL COMMENT '最后登录时间',
  `created_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='微信用户';

-- ============================================================
-- 6. 用户收藏表
-- ============================================================
CREATE TABLE IF NOT EXISTS `user_collection` (
  `id`              INT(11)  NOT NULL AUTO_INCREMENT,
  `user_id`         INT(11)  NOT NULL COMMENT '用户ID',
  `announcement_id` INT(11)  NOT NULL COMMENT '公告ID',
  `created_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_ann` (`user_id`, `announcement_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏';

-- ============================================================
-- 7. 用户订阅表
-- ============================================================
CREATE TABLE IF NOT EXISTS `user_subscription` (
  `id`          INT(11)    NOT NULL AUTO_INCREMENT,
  `user_id`     INT(11)    NOT NULL COMMENT '用户ID',
  `regions`     VARCHAR(500) DEFAULT NULL COMMENT '订阅地区（逗号分隔）',
  `categories`  VARCHAR(200) DEFAULT NULL COMMENT '订阅考试类型',
  `keywords`    VARCHAR(500) DEFAULT NULL COMMENT '订阅关键词',
  `push_enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '推送开关',
  `created_at`  DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户订阅';

-- ============================================================
-- 8. 用户考试日历表
-- ============================================================
CREATE TABLE IF NOT EXISTS `user_exam_calendar` (
  `id`              INT(11)    NOT NULL AUTO_INCREMENT,
  `user_id`         INT(11)    NOT NULL,
  `announcement_id` INT(11)    DEFAULT NULL COMMENT '关联公告ID',
  `event_name`      VARCHAR(200) NOT NULL COMMENT '事件名称',
  `event_date`      DATE       NOT NULL COMMENT '事件日期',
  `event_type`      VARCHAR(30)  NOT NULL DEFAULT 'exam' COMMENT '类型：apply_start/apply_end/exam/interview/other',
  `note`            VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `remind_before`   INT(11)    DEFAULT 0 COMMENT '提前提醒分钟数',
  `created_at`      DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_date` (`user_id`, `event_date`),
  KEY `idx_announcement_id` (`announcement_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户考试日历';

-- ============================================================
-- 9. 抓取日志表
-- ============================================================
CREATE TABLE IF NOT EXISTS `crawl_log` (
  `id`          INT(11)     NOT NULL AUTO_INCREMENT,
  `source_id`   INT(11)     NOT NULL COMMENT '数据源ID',
  `status`      VARCHAR(20) NOT NULL DEFAULT 'success' COMMENT '状态：success/partial/failed',
  `total_urls`  INT(11)     DEFAULT 0 COMMENT '发现链接数',
  `new_items`   INT(11)     DEFAULT 0 COMMENT '新增公告数',
  `updated_items` INT(11)   DEFAULT 0 COMMENT '更新公告数',
  `error_msg`   TEXT        COMMENT '错误信息',
  `duration_ms` BIGINT(20)  DEFAULT NULL COMMENT '耗时(ms)',
  `created_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_source_id` (`source_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抓取日志';

-- ============================================================
-- 10. 解析异常日志表
-- ============================================================
CREATE TABLE IF NOT EXISTS `parse_error_log` (
  `id`              INT(11)     NOT NULL AUTO_INCREMENT,
  `announcement_id` INT(11)     DEFAULT NULL COMMENT '公告ID（如果已入库）',
  `source_url`      VARCHAR(500) DEFAULT NULL COMMENT '来源URL',
  `error_type`      VARCHAR(50) NOT NULL COMMENT '异常类型：field_extract/attachment_parse/unknown',
  `error_msg`       TEXT        COMMENT '错误详情',
  `raw_snippet`     TEXT        COMMENT '原始内容片段',
  `resolved`        TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '是否已处理',
  `created_at`      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_resolved` (`resolved`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='解析异常日志';

-- ============================================================
-- 11. 系统配置表
-- ============================================================
CREATE TABLE IF NOT EXISTS `sys_config` (
  `id`          INT(11)      NOT NULL AUTO_INCREMENT,
  `config_key`  VARCHAR(100) NOT NULL COMMENT '配置键',
  `config_value` TEXT        COMMENT '配置值',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '描述',
  `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置';

-- ============================================================
-- 12. 推送记录表
-- ============================================================
CREATE TABLE IF NOT EXISTS `push_record` (
  `id`              INT(11)     NOT NULL AUTO_INCREMENT,
  `user_id`         INT(11)     NOT NULL,
  `announcement_id` INT(11)     NOT NULL,
  `push_type`       VARCHAR(30) NOT NULL DEFAULT 'subscription' COMMENT '推送类型：subscription/exam_remind',
  `status`          VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态：pending/success/failed',
  `push_content`    TEXT        COMMENT '推送内容JSON',
  `error_msg`       TEXT        COMMENT '推送失败原因',
  `retry_count`     INT(11)     DEFAULT 0,
  `created_at`      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_status` (`user_id`, `status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推送记录';

-- ============================================================
-- 初始化系统配置
-- ============================================================
INSERT INTO `sys_config` (`config_key`, `config_value`, `description`) VALUES
('crawl_interval_default', '3600', '默认抓取间隔（秒）'),
('max_push_retry', '3', '推送最大重试次数'),
('announcement_expire_days', '90', '公告过期天数（超过隐藏）');
