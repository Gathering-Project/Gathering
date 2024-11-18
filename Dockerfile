# Base 이미지로 OpenJDK 사용
FROM openjdk:17-jdk-slim

# 애플리케이션 디렉토리 생성
WORKDIR /app

# JAR 파일을 컨테이너의 /app 디렉토리로 복사
COPY build/libs/Gathering-0.0.1-SNAPSHOT.jar app.jar

# 컨테이너 실행 시 기본 명령어 설정
CMD ["java", "-jar", "app.jar"]
