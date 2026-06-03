# 微信云托管 Dockerfile - 多阶段构建（Maven构建 → Alpine运行）
FROM maven:3.6.0-jdk-8-slim as build

WORKDIR /app

COPY src /app/src
COPY settings.xml pom.xml /app/
RUN mvn -s /app/settings.xml -f /app/pom.xml clean package

FROM alpine:3.13

# 腾讯云镜像加速
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.tencent.com/g' /etc/apk/repositories \
    && apk add --update --no-cache openjdk8-jre-base \
    && rm -f /var/cache/apk/*

RUN apk add ca-certificates

WORKDIR /app
COPY --from=build /app/target/*.jar .

EXPOSE 80
CMD ["java", "-jar", "/app/springboot-wxcloudrun-1.0.jar"]
