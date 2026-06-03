# Python 爬虫容器 Dockerfile
FROM python:3.10-slim

WORKDIR /app

# 安装系统依赖（Playwright需要）
RUN apt-get update && apt-get install -y \
    libnss3 libnspr4 libatk1.0-0 libatk-bridge2.0-0 \
    libcups2 libdrm2 libxkbcommon0 libxcomposite1 \
    libxdamage1 libxfixes3 libxrandr2 libgbm1 \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install -i https://mirrors.tencent.com/pypi/simple/ -r requirements.txt
RUN playwright install chromium

COPY . .

# 定时任务入口（cron 或 supervisord）
CMD ["python", "crawler.py"]
