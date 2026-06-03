# 微信云托管 Dockerfile - 多阶段构建
FROM maven:3.6.0-jdk-8-slim as build

WORKDIR /app
COPY pom.xml .
# 先下载依赖（利用Docker层缓存）
RUN mvn dependency:go-offline -B 2>/dev/null || true

COPY src /app/src
RUN mvn clean package -DskipTests -B

FROM alpine:3.13

# 镜像加速 + 安装JRE
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.tencent.com/g' /etc/apk/repositories \
    && apk add --update --no-cache openjdk8-jre-base \
    && rm -f /var/cache/apk/*

RUN apk add ca-certificates

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 80
CMD ["java", "-jar", "/app/app.jar"]
