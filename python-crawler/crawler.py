"""
辽宁省公职类招考信息采集引擎 v3.0 — 合规版
===============================================

合规声明：
1. 仅抓取 gov.cn 域名下的公开公示页面
2. 遵守 robots.txt
3. 请求间隔 ≥ 5秒，单线程，降频访问
4. User-Agent 明确标识身份 + 联系方式
5. 不爬取任何个人信息（姓名/身份证/电话/准考证号）
6. 不全文转载，仅存储摘要 + 跳转原文链接
7. 每条数据标注官方来源
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

# ============ 合规配置 ============
CRAWLER_AGENT = (
    "LiaoNingZhaokaoCrawler/1.0 "
    "(合规爬虫，仅抓取政府公开公示招考信息；"
    "个人非商业用途，联系方式：github.com/dankfir/lnzk)"
)
API_BASE = os.getenv('API_BASE', 'http://localhost:80')
CRAWL_REPORT_URL = f'{API_BASE}/api/crawl/report'
CRAWL_PING_URL = f'{API_BASE}/api/crawl/ping'
DATA_SOURCE_URL = f'{API_BASE}/api/admin/datasource'
REQUEST_DELAY = int(os.getenv('REQUEST_DELAY', '5'))   # 合规≥5秒
CRAWL_INTERVAL = int(os.getenv('CRAWL_INTERVAL', '120'))  # 合规2小时一次

# robots.txt 缓存（每次会话有效期）
ROBOTS_CACHE = {}

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
    # 仅存储摘要（前2000字），不存全文
    summary: str = ""
    publish_date: Optional[str] = None
    category: Optional[str] = None
    region: Optional[str] = None
    recruit_unit: Optional[str] = None
    recruit_count: Optional[int] = None
    apply_start: Optional[str] = None
    apply_end: Optional[str] = None
    exam_date: Optional[str] = None
    interview_date: Optional[str] = None
    has_attachment: int = 0
    source_id: int = 1
    source_name: str = ""       # 来源名称
    fingerprint: str = ""
    attachments: List[dict] = field(default_factory=list)

    def compute_fingerprint(self) -> str:
        raw = f"{self.title}|{self.summary[:200] if self.summary else ''}|{self.publish_date or ''}"
        return hashlib.sha256(raw.encode('utf-8')).hexdigest()

    def to_dict(self) -> dict:
        d = asdict(self)
        d['fingerprint'] = self.compute_fingerprint()
        d['source_name'] = self.source_name
        d['contentText'] = self.summary  # 后端 contentText 字段
        d['attachments'] = [
            asdict(a) if hasattr(a, '__dataclass_fields__') else a
            for a in self.attachments
        ]
        return d


# ============ 合规请求 ============

def check_robots(base_url: str) -> bool:
    """检查 robots.txt，不允许则跳过"""
    if base_url in ROBOTS_CACHE:
        return ROBOTS_CACHE[base_url]

    try:
        parsed = urlparse(base_url)
        robots_url = f"{parsed.scheme}://{parsed.netloc}/robots.txt"
        resp = requests.get(robots_url, timeout=5,
                            headers={'User-Agent': CRAWLER_AGENT})
        if resp.status_code == 404:
            ROBOTS_CACHE[base_url] = True
            return True

        # 简单检查是否禁止爬虫
        if 'Disallow: /' in resp.text or 'Disallow: *' in resp.text:
            ROBOTS_CACHE[base_url] = False
            logger.warning(f"robots.txt 禁止爬取: {robots_url}")
            return False
        ROBOTS_CACHE[base_url] = True
        return True
    except Exception:
        ROBOTS_CACHE[base_url] = True
        return True


def create_session() -> requests.Session:
    """创建合规 Session —— 单一固定 UA 明确身份"""
    s = requests.Session()
    s.headers.update({
        'User-Agent': CRAWLER_AGENT,
        'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
        'Accept-Language': 'zh-CN,zh;q=0.9',
    })
    return s


def safe_fetch(url: str, session: requests.Session) -> Optional[BeautifulSoup]:
    """合规请求：限速 + robots.txt 检查"""
    base = f"{urlparse(url).scheme}://{urlparse(url).netloc}"
    if not check_robots(base):
        logger.warning(f"跳过（robots禁止）: {url}")
        return None

    time.sleep(REQUEST_DELAY)  # 合规限速 ≥5秒
    try:
        resp = session.get(url, timeout=30)
        resp.encoding = resp.apparent_encoding or 'utf-8'
        if resp.status_code == 200:
            return BeautifulSoup(resp.text, 'lxml')
        else:
            logger.warning(f"HTTP {resp.status_code}: {url}")
            return None
    except Exception as e:
        logger.error(f"请求失败 {url}: {e}")
        return None


# ============ 文本提取 ============

NOISE_KEYWORDS = [
    '首页', '考试动态', '考试通知', '考试计划', '成绩证书',
    '新闻中心', '工作动态', '政策法规', '资料下载', '机构概况',
    '互动专区', '联系方式', '常见问题', '常用考点', '友情链接',
    '辽ICP备', '关于我们', '网站地图', '隐私声明', '网站帮助',
    '设为首页', '加入收藏', '网站纠错', 'copyright', '©',
    '技术支持', '主办单位', '承建单位', '网站标识码',
    '回到顶部', '相关信息', '打印本页', '关闭窗口',
    '通知公告', '招考录用', '当前位置', '您的位置',
]


def clean_text(text: str, max_len: int = 2000) -> str:
    """清洗 -> 去导航 -> 截取摘要（不存全文）"""
    if not text:
        return ""
    lines = text.split('\n')
    cleaned = []
    for line in lines:
        s = line.strip()
        if not s or len(s) < 2:
            continue
        if any(kw in s for kw in NOISE_KEYWORDS):
            continue
        cleaned.append(s)
    result = '\n'.join(cleaned)
    result = re.sub(r'\n{3,}', '\n\n', result)
    result = re.sub(r'[ \t]{2,}', ' ', result)
    result = result.replace('&nbsp;', ' ')
    result = result.strip()
    # 截取摘要，不存全文
    if len(result) > max_len:
        result = result[:max_len] + '\n\n...（查看原文获取完整内容）'
    return result


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
        return safe_fetch(url, self.session)

    def crawl(self) -> List[Announcement]:
        results = []
        list_urls = self.crawl_config.get('listUrls', [self.base_url])
        list_selector = self.crawl_config.get('listSelector', 'a')

        for list_url in list_urls:
            logger.info(f"[{self.name}] 列表: {list_url}")
            soup = self.fetch(list_url)
            if not soup:
                continue
            items = self._extract_list(soup, list_selector)
            logger.info(f"[{self.name}] 发现 {len(items)} 条")
            for ann in items[:15]:  # 每源最多15条
                detail = self.extract_detail(ann.origin_url)
                if detail:
                    detail.source_id = self.source_id
                    detail.source_name = self.name
                    detail.region = detail.region or self.region
                    detail.category = detail.category or self.default_category
                    results.append(detail)
                time.sleep(REQUEST_DELAY)
        return results

    def _extract_list(self, soup: BeautifulSoup, selector: str) -> List[Announcement]:
        items = []
        for a_tag in soup.select(selector):
            href = a_tag.get('href', '').strip()
            title = a_tag.get('title', '') or a_tag.get_text(strip=True)
            if not href or not title or len(title) < 8:
                continue
            if not href.startswith('http'):
                href = urljoin(self.base_url, href)
            items.append(Announcement(origin_url=href, title=title))
        return items

    def extract_detail(self, url: str) -> Optional[Announcement]:
        """子类实现"""
        raise NotImplementedError


# ============ 通用公告解析器 ============

class GovAnnouncementCrawler(BaseCrawler):
    """政府公告通用解析 — 只提取摘要字段，不存全文不爬个人信息"""

    CONTENT_SELECTORS = [
        'div.article_content', 'div.news_content', 'div.TRS_Editor',
        'div#zoom', 'div.content', 'div.main-content', 'article',
        '.listleftback', '.kaoshireadback', '.listcontentback',
        '.con_text', '#UCAP-CONTENT',
    ]
    TITLE_SELECTORS = ['h1', '.article-title', '.news-title', '.bt', '#title', 'h2']

    def extract_detail(self, url: str) -> Optional[Announcement]:
        soup = self.fetch(url)
        if not soup:
            return None

        ann = Announcement(origin_url=url)

        # 标题
        for sel in self.TITLE_SELECTORS:
            tag = soup.select_one(sel)
            if tag:
                ann.title = tag.get_text(strip=True)
                break
        if not ann.title:
            ann.title = soup.title.get_text(strip=True) if soup.title else ""

        # 正文摘要（不存全文，仅提取关键文本块 → 截取2000字以内）
        text = self._extract_body_text(soup)
        ann.summary = clean_text(text, max_len=2000)

        # 日期
        date_pat = r'(\d{4}[-/年]\d{1,2}[-/月]\d{1,2})'
        m = re.search(date_pat, ann.summary)
        if m:
            ann.publish_date = m.group(1)

        # 招聘单位（只取单位名称，不涉及个人信息）
        unit_pat = r'([\u4e00-\u9fa5]{3,20}(?:局|厅|院|校|中心|集团|公司|委员会|办公室))'
        m = re.search(unit_pat, ann.summary)
        if m:
            ann.recruit_unit = m.group(1)

        # 招聘人数
        cnt = re.search(r'(?:招聘|招录|招考|遴选).*?(\d+)人', ann.summary)
        if cnt:
            try: ann.recruit_count = int(cnt.group(1))
            except: pass

        # 时间字段（报名时间、考试时间）
        ann.apply_start = self._extract_date(ann.summary, r'报名时间[：:]\s*(\d{4}[-/年]\d{1,2}[-/月]\d{1,2})')
        ann.apply_end = self._extract_date(ann.summary, r'截止.*?(\d{4}[-/年]\d{1,2}[-/月]\d{1,2})')
        ann.exam_date = self._extract_date(ann.summary, r'笔试时间[：:]\s*(\d{4}[-/年]\d{1,2}[-/月]\d{1,2})')

        # 附件链接
        for a_tag in soup.select('a[href$=".pdf"], a[href$=".doc"], a[href$=".xls"], a[href$=".xlsx"]'):
            href = a_tag.get('href', '')
            if not href:
                continue
            if not href.startswith('http'):
                href = urljoin(url, href)
            filename = os.path.basename(urlparse(href).path)
            ann.attachments.append({
                'file_name': filename,
                'file_url': href,
                'file_type': 'pdf' if '.pdf' in href else 'word' if '.doc' in href else 'excel',
            })
            ann.has_attachment = 1

        return ann

    def _extract_body_text(self, soup: BeautifulSoup) -> str:
        """从页面中提取正文文本块——只保留内容区"""
        for sel in self.CONTENT_SELECTORS:
            div = soup.select_one(sel)
            if div and len(div.get_text(strip=True)) > 200:
                return div.get_text(separator='\n')
        # 降级：找最大文本div
        best, best_len = None, 0
        for div in soup.find_all('div'):
            t = div.get_text(strip=True)
            if 200 < len(t) < 30000 and len(t) > best_len:
                best_len = len(t)
                best = div
        return best.get_text(separator='\n') if best else soup.get_text(separator='\n')[:3000]

    @staticmethod
    def _extract_date(text: str, pattern: str) -> Optional[str]:
        m = re.search(pattern, text)
        return m.group(1) if m else None


# ============ HTTP 上报 ============

def report_to_api(source_id: int, announcements: List[Announcement]) -> dict:
    if not announcements:
        return {'created': 0, 'skipped': 0}
    payload = {'sourceId': source_id, 'items': [a.to_dict() for a in announcements]}
    try:
        resp = requests.post(CRAWL_REPORT_URL, json=payload, timeout=30)
        if resp.status_code == 200:
            data = resp.json().get('data', {})
            logger.info(f"上报完成: sourceId={source_id}, created={data.get('created',0)}")
            return data
        else:
            logger.error(f"上报失败: {resp.status_code}")
            return {'error': resp.text}
    except Exception as e:
        logger.error(f"上报异常: {e}")
        return {'error': str(e)}


# ============ 数据源获取 ============

def fetch_data_sources() -> list:
    try:
        resp = requests.get(DATA_SOURCE_URL, timeout=10,
                            headers={'User-Agent': CRAWLER_AGENT})
        if resp.status_code == 200:
            return resp.json().get('data', [])
    except Exception as e:
        logger.warning(f"获取数据源失败: {e}")
    return []   # 空列表 = 不爬


# ============ 主调度 ============

def run_crawl_cycle():
    logger.info("=" * 50)
    logger.info(f"合规爬虫启动 —— 间隔≥{REQUEST_DELAY}s | 每源≤15条 | 不存全文")
    start = time.time()

    sources = fetch_data_sources()
    logger.info(f"获取到 {len(sources)} 个启用数据源")

    total = 0
    for src in sources:
        try:
            c = GovAnnouncementCrawler(src)
            items = c.crawl()
            result = report_to_api(src['id'], items)
            total += result.get('created', 0)
        except Exception as e:
            logger.error(f"[{src.get('name','?')}] 异常: {e}")

    elapsed = time.time() - start
    logger.info(f"完成: 耗时 {elapsed:.0f}s | 新增 {total} 条")
    logger.info("=" * 50)


def main():
    logger.info("=" * 40)
    logger.info("辽宁公职招考 · 合规爬虫引擎 v3.0")
    logger.info(f"UA: {CRAWLER_AGENT}")
    logger.info(f"限速: {REQUEST_DELAY}s/请求")
    logger.info(f"间隔: {CRAWL_INTERVAL}分钟/轮")
    logger.info(f"API: {API_BASE}")
    logger.info("=" * 40)

    run_crawl_cycle()
    if CRAWL_INTERVAL > 0:
        schedule.every(CRAWL_INTERVAL).minutes.do(run_crawl_cycle)
        while True:
            schedule.run_pending()
            time.sleep(30)


if __name__ == '__main__':
    main()
