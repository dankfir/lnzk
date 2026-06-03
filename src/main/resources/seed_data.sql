-- ============================================================
-- 辽宁省公职类招考数据源种子数据
-- 部署后在 MySQL 中执行此文件即可初始化
-- ============================================================

-- 1. 辽宁省人事考试网（公务员 + 事业单位）
INSERT INTO `data_source` (`name`, `url`, `type`, `region`, `category`, `crawl_config`, `status`, `crawl_interval`) VALUES
('辽宁省人事考试网-公务员', 'https://www.lnrsks.com', 'gov_website', '辽宁省', 'gwy',
 '{"listUrls":["https://www.lnrsks.com/html/kaoshidongtai/gwy_zhaokaogonggao/"],"listSelector":"ul.news_list li a","detailContentSelector":"div.article_content"}',
 1, 3600);

-- 2. 辽宁省人事考试网（事业单位）
INSERT INTO `data_source` (`name`, `url`, `type`, `region`, `category`, `crawl_config`, `status`, `crawl_interval`) VALUES
('辽宁省人事考试网-事业单位', 'https://www.lnrsks.com', 'gov_website', '辽宁省', 'sydw',
 '{"listUrls":["https://www.lnrsks.com/html/kaoshidongtai/sydw_zhaokaogonggao/"],"listSelector":"ul.news_list li a","detailContentSelector":"div.article_content"}',
 1, 3600);

-- 3. 辽宁人事人才公共服务网
INSERT INTO `data_source` (`name`, `url`, `type`, `region`, `category`, `crawl_config`, `status`, `crawl_interval`) VALUES
('辽宁人事人才公共服务网', 'https://www.lnrc.com.cn', 'gov_website', '辽宁省', 'sydw',
 '{"listUrls":["https://www.lnrc.com.cn/news/notice/"],"listSelector":"div.news_list a","detailContentSelector":"div.news_content"}',
 1, 7200);

-- 4. 沈阳市人力资源和社会保障局
INSERT INTO `data_source` (`name`, `url`, `type`, `region`, `category`, `crawl_config`, `status`, `crawl_interval`) VALUES
('沈阳市人力资源和社会保障局', 'http://rsj.shenyang.gov.cn', 'gov_website', '沈阳', 'sydw',
 '{"listUrls":["http://rsj.shenyang.gov.cn/zwgk/zdlyxxgk/gwyzk/","http://rsj.shenyang.gov.cn/zwgk/zdlyxxgk/sydwzk/"],"listSelector":"ul.list li a","detailContentSelector":"div.TRS_Editor"}',
 1, 3600);

-- 5. 大连市人力资源和社会保障局
INSERT INTO `data_source` (`name`, `url`, `type`, `region`, `category`, `crawl_config`, `status`, `crawl_interval`) VALUES
('大连市人力资源和社会保障局', 'https://rsj.dl.gov.cn', 'gov_website', '大连', 'sydw',
 '{"listUrls":["https://rsj.dl.gov.cn/col/col5606/index.html","https://rsj.dl.gov.cn/col/col5608/index.html"],"listSelector":"ul.list li a","detailContentSelector":"div.article_content"}',
 1, 3600);

-- 6. 辽宁省教育厅（教师招聘）
INSERT INTO `data_source` (`name`, `url`, `type`, `region`, `category`, `crawl_config`, `status`, `crawl_interval`) VALUES
('辽宁省教育厅', 'http://jyt.ln.gov.cn', 'gov_website', '辽宁省', 'jiaoshi',
 '{"listUrls":["http://jyt.ln.gov.cn/zwgk/gsgg/","http://jyt.ln.gov.cn/zwgk/tzgg/"],"listSelector":"ul.list li a","detailContentSelector":"div.TRS_Editor"}',
 1, 3600);

-- 7. 辽宁省卫生健康委员会（医疗招聘）
INSERT INTO `data_source` (`name`, `url`, `type`, `region`, `category`, `crawl_config`, `status`, `crawl_interval`) VALUES
('辽宁省卫生健康委员会', 'http://wsjk.ln.gov.cn', 'gov_website', '辽宁省', 'yiliao',
 '{"listUrls":["http://wsjk.ln.gov.cn/wst_xxgk/wst_zwgk/wst_gsgg/"],"listSelector":"div.list-right ul li a","detailContentSelector":"div.con"}',
 1, 3600);
