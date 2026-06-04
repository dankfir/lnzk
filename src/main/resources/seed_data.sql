-- ============================================================
-- 辽宁省公职类招考数据源种子数据 v2.0
-- 包含已验证可通的数据源 + 各地市人社局
-- ============================================================

-- 1. 辽宁省人事考试网-公务员 ✅ 已验证
INSERT INTO `data_source` (`name`, `url`, `type`, `region`, `category`, `crawl_config`, `status`, `crawl_interval`) VALUES
('辽宁省人事考试网-公务员', 'https://www.lnrsks.com', 'gov_website', '辽宁省', 'gwy',
 '{"listUrls":["https://www.lnrsks.com/html/kaoshidongtai/gwy_zhaokaogonggao/"],"listSelector":".listleftback a","detailContentSelector":".listleftback"}',
 1, 3600);

-- 2. 辽宁省人事考试网-事业单位 ✅ 已验证
INSERT INTO `data_source` (`name`, `url`, `type`, `region`, `category`, `crawl_config`, `status`, `crawl_interval`) VALUES
('辽宁省人事考试网-事业单位', 'https://www.lnrsks.com', 'gov_website', '辽宁省', 'sydw',
 '{"listUrls":["https://www.lnrsks.com/html/kaoshidongtai/sydw_zhaokaogonggao/"],"listSelector":".listleftback a","detailContentSelector":".listleftback"}',
 1, 3600);

-- 3. 辽宁省教育厅-教师招聘 ✅ 已验证
INSERT INTO `data_source` (`name`, `url`, `type`, `region`, `category`, `crawl_config`, `status`, `crawl_interval`) VALUES
('辽宁省教育厅', 'http://jyt.ln.gov.cn', 'gov_website', '辽宁省', 'jiaoshi',
 '{"listUrls":["http://jyt.ln.gov.cn/jyt/gk/gsgg/"],"listSelector":"a","detailContentSelector":""}',
 1, 3600);

-- 4. 辽宁省卫健委-医疗招聘 ✅ 已验证
INSERT INTO `data_source` (`name`, `url`, `type`, `region`, `category`, `crawl_config`, `status`, `crawl_interval`) VALUES
('辽宁省卫生健康委员会', 'http://wsjk.ln.gov.cn', 'gov_website', '辽宁省', 'yiliao',
 '{"listUrls":["http://wsjk.ln.gov.cn/wsjk/index/gsgg/"],"listSelector":"a","detailContentSelector":""}',
 1, 3600);

-- 5. 鞍山市人力资源和社会保障局 ✅ 已验证
INSERT INTO `data_source` (`name`, `url`, `type`, `region`, `category`, `crawl_config`, `status`, `crawl_interval`) VALUES
('鞍山市人力资源和社会保障局', 'http://rsj.anshan.gov.cn', 'gov_website', '鞍山', 'sydw',
 '{"listUrls":["http://rsj.anshan.gov.cn/assrlzyhshbzj/tzgg/glist.html"],"listSelector":"a","detailContentSelector":""}',
 1, 7200);

-- 6. 本溪市人力资源和社会保障局（需验证选择器）
INSERT INTO `data_source` (`name`, `url`, `type`, `region`, `category`, `crawl_config`, `status`, `crawl_interval`) VALUES
('本溪市人力资源和社会保障局', 'http://rsj.benxi.gov.cn', 'gov_website', '本溪', 'sydw',
 '{"listUrls":["http://rsj.benxi.gov.cn/xwzx/gsgg"],"listSelector":"a","detailContentSelector":""}',
 0, 7200);

-- 7. 大连市人力资源和社会保障局（需调试选择器）
INSERT INTO `data_source` (`name`, `url`, `type`, `region`, `category`, `crawl_config`, `status`, `crawl_interval`) VALUES
('大连市人力资源和社会保障局', 'https://rsj.dl.gov.cn', 'gov_website', '大连', 'sydw',
 '{"listUrls":["https://rsj.dl.gov.cn/col/col4341/index.html"],"listSelector":"a","detailContentSelector":""}',
 0, 7200);

-- 8. 辽阳市人力资源和社会保障局
INSERT INTO `data_source` (`name`, `url`, `type`, `region`, `category`, `crawl_config`, `status`, `crawl_interval`) VALUES
('辽阳市人力资源和社会保障局', 'http://rsj.liaoyang.gov.cn', 'gov_website', '辽阳', 'sydw',
 '{"listUrls":["http://rsj.liaoyang.gov.cn/column_list.html?categorynum=008&deptcode=001"],"listSelector":"a","detailContentSelector":""}',
 0, 7200);

-- 9. 朝阳市人力资源和社会保障局
INSERT INTO `data_source` (`name`, `url`, `type`, `region`, `category`, `crawl_config`, `status`, `crawl_interval`) VALUES
('朝阳市人力资源和社会保障局', 'http://rsj.chaoyang.gov.cn', 'gov_website', '朝阳', 'sydw',
 '{"listUrls":["http://rsj.chaoyang.gov.cn/cyszf/ywdt/tzgg/glist.html"],"listSelector":"a","detailContentSelector":""}',
 0, 7200);

-- 10. 葫芦岛市人力资源和社会保障局
INSERT INTO `data_source` (`name`, `url`, `type`, `region`, `category`, `crawl_config`, `status`, `crawl_interval`) VALUES
('葫芦岛市人力资源和社会保障局', 'http://rsj.hld.gov.cn', 'gov_website', '葫芦岛', 'sydw',
 '{"listUrls":["http://rsj.hld.gov.cn/ywdt/tzgg/"],"listSelector":"a","detailContentSelector":""}',
 0, 7200);
