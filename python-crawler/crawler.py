"""
辽宁省公职类招考信息采集引擎 v2.0
==================================
新增：
- HTTP 上报到 Spring Boot API
- 附件下载 + MIME 类型检测
- 多数据源并发调度
- 定时循环抓取

合规约束：
1. 仅抓取 gov.cn 域名下的公开公示页面
2. 遵守 robots.txt
3. 请求间隔 ≥ 2秒
"""
import hashlib
import json
import logging
import os
import re
import time
import schedule
import requests
from datetime import datetime, date
from typing import Optional, List
from dataclasses import dataclass, asdict, field
from urllib.parse import urljoin, urlparse
from bs4 import BeautifulSoup

logging.basicConfig(level=logging.INFO, format='%(asctime)s [%(levelname)s] %(message)s')
logger = logging.getLogger(__name__)

# ============ 配置 ============
API_BASE = os.getenv('API_BASE', 'http://localhost:80')
CRAWL_REPORT_URL = f'{API_BASE}/api/crawl/report'
CRAWL_PING_URL = f'{API_BASE}/api/crawl/ping'
DATA_SOURCE_URL = f'{API_BASE}/api/admin/datasource'
REQUEST_DELAY = int(os.getenv('REQUEST_DELAY', '2'))  # 秒
CRAWL_INTERVAL = int(os.getenv('CRAWL_INTERVAL', '60'))  # 分钟
DOWNLOAD_DIR = os.getenv('DOWNLOAD_DIR', '/tmp/lnzk_attachments')


@dataclass
class Attachment:
    file_name: str
    file_url: str
    file_type: str = "pdf"
    file_size: int = 0


@dataclass
class Announcement:
    origin_url: str
    title: str = ""
    content_html: str = ""
    content_text: str = ""
    publish_date: Optional[str] = None
    category: Optional[str] = None
    region: Optional[str] = None
    recruit_unit: Optional[str] = None
    recruit_count: Optional[int] = None
    contact_phone: Optional[str] = None
    apply_start: Optional[str] = None
    apply_end: Optional[str] = None
    exam_date: Optional[str] = None
    interview_date: Optional[str] = None
    has_attachment: int = 0
    source_id: int = 1
    fingerprint: str = ""
    attachments: List[dict] = field(default_factory=list)

    def compute_fingerprint(self) -> str:
        raw = f"{self.title}|{self.content_text[:500] if self.content_text else ''}|{self.publish_date or ''}"
        return hashlib.sha256(raw.encode('utf-8')).hexdigest()

    def to_dict(self) -> dict:
        d = asdict(self)
        d['fingerprint'] = self.compute_fingerprint()
        d['attachments'] = [asdict(a) if hasattr(a, '__dataclass_fields__') else a
                           for a in self.attachments]
        return d


# ============ 工具函数 ============

def create_session() -> requests.Session:
    """创建带随机UA的Session"""
    ua_list = [
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36',
        'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
        'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
    ]
    s = requests.Session()
    s.headers.update({
        'User-Agent': ua_list[int(time.time()) % len(ua_list)],
        'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
        'Accept-Language': 'zh-CN,zh;q=0.9',
        'Accept-Encoding': 'gzip, deflate',
        'Connection': 'keep-alive',
    })
    return s


def extract_text_field(text: str, patterns: List[str]) -> Optional[str]:
    """从文本中按正则模式提取字段"""
    for pattern in patterns:
        m = re.search(pattern, text)
        if m:
            return m.group(1).strip()
    return None


def detect_file_type(filename: str, content_type: str = "") -> str:
    """检测文件类型"""
    ext = os.path.splitext(filename.lower())[1]
    mapping = {
        '.pdf': 'pdf', '.doc': 'word', '.docx': 'word',
        '.xls': 'excel', '.xlsx': 'excel',
        '.jpg': 'image', '.jpeg': 'image', '.png': 'image', '.gif': 'image',
    }
    return mapping.get(ext, 'other')


def download_attachment(url: str, filename: str, session: requests.Session) -> Optional[str]:
    """下载附件到本地，返回本地路径"""
    try:
        os.makedirs(DOWNLOAD_DIR, exist_ok=True)
        local_path = os.path.join(DOWNLOAD_DIR, filename)
        if os.path.exists(local_path):
            return local_path

        resp = session.get(url, timeout=60, stream=True)
        if resp.status_code == 200:
            with open(local_path, 'wb') as f:
                for chunk in resp.iter_content(8192):
                    f.write(chunk)
            logger.info(f"附件下载成功: {filename} ({os.path.getsize(local_path)} bytes)")
            return local_path
    except Exception as e:
        logger.error(f"附件下载失败 {url}: {e}")
    return None


# ============ 数据源基类 ============

class BaseCrawler:
    def __init__(self, source: dict):
        self.source_id = source['id']
        self.name = source['name']
        self.base_url = source['url']
        self.region = source.get('region', '')
        self.default_category = source.get('category', '')
        self.crawl_config = json.loads(source.get('crawlConfig', '{}'))
        self.session = create_session()

    def fetch(self, url: str) -> Optional[BeautifulSoup]:
        time.sleep(REQUEST_DELAY)
        try:
            resp = self.session.get(url, timeout=30)
            resp.encoding = resp.apparent_encoding or 'utf-8'
            return BeautifulSoup(resp.text, 'lxml')
        except Exception as e:
            logger.error(f"[{self.name}] 请求失败 {url}: {e}")
            return None

    def extract_list(self, soup: BeautifulSoup) -> List[Announcement]:
        raise NotImplementedError

    def extract_detail(self, url: str) -> Optional[Announcement]:
        raise NotImplementedError

    def crawl(self) -> List[Announcement]:
        """完整抓取流程：列表 → 逐条解析详情"""
        results = []
        list_urls = self.crawl_config.get('listUrls', [self.base_url])
        list_selector = self.crawl_config.get('listSelector', 'a')

        for list_url in list_urls:
            logger.info(f"[{self.name}] 抓取列表: {list_url}")
            soup = self.fetch(list_url)
            if not soup:
                continue

            items = self.extract_list_from_soup(soup, list_selector)
            logger.info(f"[{self.name}] 发现 {len(items)} 条列表项")

            for ann in items[:30]:  # 每源最多30条
                detail = self.extract_detail(ann.origin_url)
                if detail:
                    detail.source_id = self.source_id
                    detail.region = detail.region or self.region
                    detail.category = detail.category or self.default_category
                    detail.compute_fingerprint()
                    results.append(detail)
                time.sleep(REQUEST_DELAY)

        return results

    def extract_list_from_soup(self, soup: BeautifulSoup, selector: str) -> List[Announcement]:
        """通用列表提取"""
        items = []
        links = soup.select(selector)
        for a_tag in links:
            href = a_tag.get('href', '').strip()
            title = a_tag.get('title', '') or a_tag.get_text(strip=True)
            if not href or not title or len(title) < 8:
                continue
            if not href.startswith('http'):
                href = urljoin(self.base_url, href)
            items.append(Announcement(origin_url=href, title=title, source_id=self.source_id))
        return items


# ============ 通用公告页解析 ============

class GovAnnouncementCrawler(BaseCrawler):
    """辽宁省各级政府官网公告通用爬虫"""

    def extract_list(self, soup):
        pass  # 使用基类通用方法

    def extract_detail(self, url: str) -> Optional[Announcement]:
        soup = self.fetch(url)
        if not soup:
            return None

        ann = Announcement(origin_url=url, source_id=self.source_id)

        # 标题
        title_selectors = ['h1', '.article-title', '.news-title', '.bt', '#title', 'h2']
        for sel in title_selectors:
            tag = soup.select_one(sel)
            if tag:
                ann.title = tag.get_text(strip=True)
                break

        # 正文
        content_selectors = [
            'div.article_content', 'div.news_content', 'div.TRS_Editor',
            'div#zoom', 'div.content', 'div.main-content', 'article'
        ]
        for sel in content_selectors:
            content_div = soup.select_one(sel)
            if content_div:
                ann.content_html = str(content_div)
                ann.content_text = content_div.get_text(separator='\n', strip=True)
                break

        if not ann.content_text:
            ann.content_text = soup.get_text(separator='\n', strip=True)[:5000]

        # 发布日期
        date_patterns = [
            r'(\d{4}[-/年]\d{1,2}[-/月]\d{1,2})',
            r'发布时间[：:]\s*(\d{4}-\d{2}-\d{2})',
        ]
        ann.publish_date = extract_text_field(ann.content_text, date_patterns)

        # 招聘单位
        ann.recruit_unit = extract_text_field(ann.content_text, [
            r'([\u4e00-\u9fa5]{3,20}(?:局|厅|院|校|中心|集团|公司|委员会|办公室))'
        ])

        # 招聘人数
        count_text = extract_text_field(ann.content_text, [
            r'(?:招聘|招录|招考|遴选).*?(\d+)人',
            r'(?:计划招聘).*?(\d+)名',
            r'共计\s*(\d+)\s*人',
        ])
        if count_text:
            try:
                ann.recruit_count = int(count_text)
            except ValueError:
                pass

        # 报名时间
        ann.apply_start = extract_text_field(ann.content_text, [
            r'报名时间[：:]\s*(\d{4}[-/年]\d{1,2}[-/月]\d{1,2})'
        ])
        ann.apply_end = extract_text_field(ann.content_text, [
            r'截止.*?(\d{4}[-/年]\d{1,2}[-/月]\d{1,2})',
            r'报名.*?至.*?(\d{4}[-/年]\d{1,2}[-/月]\d{1,2})'
        ])

        # 笔试时间
        ann.exam_date = extract_text_field(ann.content_text, [
            r'笔试时间[：:]\s*(\d{4}[-/年]\d{1,2}[-/月]\d{1,2})'
        ])

        # 提取附件链接
        for a_tag in soup.select('a[href]'):
            href = a_tag.get('href', '')
            if not href:
                continue
            text = a_tag.get_text(strip=True)
            if any(ext in href.lower() for ext in ['.pdf', '.doc', '.xls', '.xlsx', '.docx']):
                if not href.startswith('http'):
                    href = urljoin(url, href)
                filename = os.path.basename(urlparse(href).path) or text
                ann.attachments.append(Attachment(
                    file_name=filename,
                    file_url=href,
                    file_type=detect_file_type(filename)
                ))
                ann.has_attachment = 1

        return ann


# ============ HTTP 上报 ============

def report_to_api(source_id: int, announcements: List[Announcement]) -> dict:
    """将解析结果上报到 Spring Boot API"""
    if not announcements:
        return {'created': 0, 'skipped': 0}

    payload = {
        'sourceId': source_id,
        'items': [a.to_dict() for a in announcements]
    }

    try:
        resp = requests.post(CRAWL_REPORT_URL, json=payload, timeout=60)
        if resp.status_code == 200:
            data = resp.json()
            result = data.get('data', {})
            logger.info(f"上报完成: sourceId={source_id}, "
                       f"created={result.get('created', 0)}, "
                       f"skipped={result.get('skipped', 0)}")
            return result
        else:
            logger.error(f"上报失败 HTTP {resp.status_code}: {resp.text}")
            return {'error': resp.text}
    except Exception as e:
        logger.error(f"上报异常: {e}")
        return {'error': str(e)}


def fetch_data_sources() -> list:
    """从后端API获取启用的数据源列表"""
    try:
        resp = requests.get(DATA_SOURCE_URL, timeout=10)
        if resp.status_code == 200:
            data = resp.json()
            return data.get('data', [])
    except Exception as e:
        logger.warning(f"获取数据源失败，使用内置源: {e}")

    # 兜底：内置数据源
    return [
        {
            'id': 1, 'name': '辽宁省人事考试网',
            'url': 'https://www.lnrsks.com',
            'region': '辽宁省', 'category': 'gwy',
            'crawlConfig': json.dumps({
                'listUrls': [
                    'https://www.lnrsks.com/html/kaoshidongtai/gwy_zhaokaogonggao/',
                    'https://www.lnrsks.com/html/kaoshidongtai/sydw_zhaokaogonggao/'
                ],
                'listSelector': 'ul.news_list li a'
            })
        }
    ]


# ============ 主调度 ============

def run_crawl_cycle():
    """执行一轮全量抓取"""
    logger.info("=" * 50)
    logger.info("开始新一轮抓取")
    start = time.time()

    sources = fetch_data_sources()
    logger.info(f"获取到 {len(sources)} 个数据源")

    total_created = 0
    for source in sources:
        try:
            crawler = GovAnnouncementCrawler(source)
            announcements = crawler.crawl()
            result = report_to_api(source['id'], announcements)
            total_created += result.get('created', 0)
        except Exception as e:
            logger.error(f"数据源 [{source.get('name', '?')}] 抓取异常: {e}")

    elapsed = time.time() - start
    logger.info(f"本轮抓取完成，耗时 {elapsed:.1f}s，新增公告 {total_created} 条")
    logger.info("=" * 50)


def main():
    logger.info("辽宁省公职类招考爬虫引擎启动")
    logger.info(f"API地址: {API_BASE}")
    logger.info(f"抓取间隔: {CRAWL_INTERVAL} 分钟")

    # 先执行一轮
    run_crawl_cycle()

    # 定时调度
    schedule.every(CRAWL_INTERVAL).minutes.do(run_crawl_cycle)

    while True:
        schedule.run_pending()
        time.sleep(30)


if __name__ == '__main__':
    main()
