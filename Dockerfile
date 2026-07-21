# ---- Build Stage: Maven 构建 ----
FROM maven:3.6.3-jdk-8 AS builder
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn package -DskipTests -B -q

# ---- Run Stage: 运行 jar ----
FROM openjdk:8u312-jdk-bullseye

RUN mkdir /app \
  && mkdir /app/springboot \
  && mkdir /app/appsystems \
  && mkdir /app/applogs \
  && mkdir /app/appsystems/config \
  && mkdir /app/appsystems/qr \
  && mkdir /app/applogs/dump \
  && mkdir /app/appsystems/tmp

COPY src/main/resources/start.sh /app/start.sh
COPY --from=builder /build/target/*.jar /app/appsystems/

RUN chmod +x /app/start.sh

WORKDIR /app/appsystems

ENTRYPOINT ["/bin/sh", "-c","/app/start.sh"]
