# 辽宁省公职类招考信息聚合小程序

基于微信云托管 + Spring Boot + 微信原生小程序 + Python 爬虫的全栈项目。

## 项目结构

```
lnzk/                          # 后端 Spring Boot
├── pom.xml                    # Maven 依赖（Spring Boot 2.5.5 + MyBatis + Redis）
├── Dockerfile                  # 多阶段构建镜像
├── container.config.json       # 云托管配置
├── settings.xml                # Maven 阿里云镜像
├── src/main/java/.../wxcloudrun/
│   ├── WxCloudRunApplication.java
│   ├── config/                 # Redis/CORS/微信SDK/ApiResponse
│   ├── model/                  # 10 个实体
│   ├── dao/                    # 9 个 MyBatis Mapper
│   ├── dto/                    # 8 个请求/响应 DTO
│   ├── service/                # 8 个接口 + 8 个实现
│   ├── controller/             # 8 个 REST Controller
│   └── task/                   # 3 个定时任务
├── src/main/resources/
│   ├── application.yml         # 环境变量注入
│   ├── db.sql                  # 12张表 DDL
│   ├── seed_data.sql           # 7个辽宁省数据源种子数据
│   ├── mapper/                 # 9个 MyBatis XML
│   └── static/index.html       # 后台管理页面
└── python-crawler/             # Python 爬虫（独立容器）
    ├── crawler.py               # 采集引擎 + HTTP上报
    ├── requirements.txt
    └── Dockerfile

lnzk-f/                        # 微信小程序前端
├── miniprogram/
│   ├── app.js / app.json / app.wxss
│   ├── config.js               # 后端API地址
│   ├── utils/api.js            # 请求封装
│   └── pages/
│       ├── home/               # 首页（轮播/分类/城市/最新）
│       ├── search/             # 检索（多维筛选）
│       ├── detail/             # 详情（时间轴/收藏/分享）
│       └── mine/               # 个人中心（登录/订阅）
```

## 快速部署

### 1. 微信云托管 — 后端

```bash
# 在微信云托管控制台创建服务，选择「代码仓库部署」
# 关联 lnzk 仓库，框架选择「Spring Boot」

# 环境变量（在「服务设置」中配置）：
MYSQL_ADDRESS=<云托管MySQL内网地址>
MYSQL_USERNAME=root
MYSQL_PASSWORD=YaNbZpf8
MYSQL_DATABASE=lnzk
REDIS_HOST=<云托管Redis内网地址>
REDIS_PORT=6379
REDIS_PASSWORD=
WX_APPID=wx226765169edccdc8
WX_SECRET=<你的小程序secret>

# 部署后访问 http://<域名>/ 进入后台管理
# 访问 http://<域名>/api/crawl/ping 确认API可用
```

### 2. 数据库初始化

```bash
# 方式A：在云托管MySQL控制台导入
# 依次执行 src/main/resources/db.sql（建表）
# 然后执行 src/main/resources/seed_data.sql（种子数据）

# 方式B：通过命令行
mysql -h <MYSQL_ADDRESS> -u root -p lnzk < src/main/resources/db.sql
mysql -h <MYSQL_ADDRESS> -u root -p lnzk < src/main/resources/seed_data.sql
```

### 3. Python 爬虫 — 独立容器

```bash
cd python-crawler

# 环境变量
export API_BASE=https://<你的云托管域名>
export REQUEST_DELAY=3
export CRAWL_INTERVAL=60

pip install -r requirements.txt
python crawler.py
```

### 4. 微信小程序

```bash
# 1. 用微信开发者工具打开 lnzk-f 目录
# 2. 修改 miniprogram/config.js 中的 baseUrl 为云托管域名
# 3. 在小程序后台配置 request 合法域名
# 4. TabBar图标：放入 miniprogram/images/ 目录
#    - tab-home.png / tab-home-active.png
#    - tab-search.png / tab-search-active.png
#    - tab-mine.png / tab-mine-active.png
```

## API 清单

### 用户
| 端点 | 方法 | 说明 |
|------|------|------|
| `/api/auth/login` | POST | 微信 code2session 登录 |

### 公告
| 端点 | 方法 | 说明 |
|------|------|------|
| `/api/announcement/home` | GET | 首页聚合数据 |
| `/api/announcement/latest` | GET | 最新公告 `?limit=10` |
| `/api/announcement/search` | POST | 多条件检索 |
| `/api/announcement/{id}` | GET | 公告详情 |

### 订阅
| 端点 | 方法 | 说明 |
|------|------|------|
| `/api/subscription/{userId}` | GET/POST/DELETE | 订阅管理 |

### 爬虫
| 端点 | 方法 | 说明 |
|------|------|------|
| `/api/crawl/report` | POST | 爬虫上报数据 |
| `/api/crawl/ping` | GET | 心跳检测 |

### 后台管理
| 端点 | 方法 | 说明 |
|------|------|------|
| `/api/admin/datasource` | GET/POST | 数据源列表/新增 |
| `/api/admin/datasource/{id}` | GET/PUT | 数据源详情/编辑 |
| `/api/admin/datasource/{id}/status` | PUT | 启用/停用 |
| `/api/admin/datasource/{id}/test` | POST | 连通性测试 |
| `/api/admin/push/records` | GET | 推送记录 |
| `/api/admin/push/stats` | GET | 推送统计 |
| `/api/admin/push/retry` | POST | 重试失败推送 |
| `GET /` | - | 后台管理页面 |

### 定时任务
| 任务 | 频率 | 说明 |
|------|------|------|
| `crawlHealthCheck` | 每30分钟 | 数据源健康检查告警 |
| `expireAnnouncements` | 每天3:00 | 下架90天前公告 |
| `retryFailedPush` | 每10分钟 | 重试失败推送 |

## 微信审核准备

1. **订阅消息模板**：在微信公众平台申请模板（关键词: thing1~thing5）
2. **类目选择**：「工具 → 信息查询」（无需特殊资质）
3. **服务器域名**：在小程序后台添加云托管域名为 request 合法域名
4. **AppSecret**：从小程序后台获取，填入云托管环境变量 `WX_SECRET`

## 技术栈

- **后端**: Java 8 / Spring Boot 2.5.5 / MyBatis / Redis
- **小程序**: 微信原生框架
- **爬虫**: Python 3 / BeautifulSoup4 / requests / schedule
- **数据库**: MySQL 5.7
- **部署**: 微信云托管 Docker 容器
