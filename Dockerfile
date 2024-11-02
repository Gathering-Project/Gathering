# Base 이미지로 OpenJDK 사용
#FROM openjdk:17-jdk-slim
FROM --platform=linux/arm64 openjdk:17-jdk-slim
# 애플리케이션 디렉토리 생성
WORKDIR /app

# JAR 파일을 컨테이너의 /app 디렉토리로 복사
COPY build/libs/Gathering-0.0.1-SNAPSHOT.jar app.jar

# S3에서 .env 파일 다운로드 후 환경 변수 설정 스크립트 복사
#COPY entrypoint.sh /app/entrypoint.sh
#RUN chmod +x /app/entrypoint.sh

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
#ENTRYPOINT ["/app/entrypoint.sh", "java", "-jar", "/app/app.jar"]