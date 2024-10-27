# Base 이미지로 OpenJDK 사용
FROM openjdk:17-jdk-slim

# 애플리케이션 디렉토리 생성
WORKDIR /app

# JAR 파일을 컨테이너의 /app 디렉토리로 복사
COPY build/libs/Gathering-0.0.1-SNAPSHOT.jar app.jar

# 애플리케이션 실행
ENTRYPOINT ["java","-jar","/app/app.jar"]


#FROM jenkins/jenkins:lts
#
#USER root
#
## AWS CLI 설치
#RUN apt-get update && \
#    apt-get install -y awscli && \
#    apt-get clean
#
#USER jenkins