FROM openjdk:8u312-jdk-bullseye

RUN mkdir /app  \
  && mkdir /app/springboot \
  && mkdir /app/appsystems \
  && mkdir /app/applogs \
  && mkdir /app/appsystems/config \
  && mkdir /app/appsystems/qr \
  && mkdir /app/applogs/dump \
  && mkdir /app/appsystems/tmp

COPY start.sh /app/start.sh
COPY *.jar /app/appsystems/


RUN chmod +x /app/start.sh

WORKDIR /app/appsystems

ENTRYPOINT ["/bin/sh", "-c","/app/start.sh"]