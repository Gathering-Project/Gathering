# Base 이미지로 OpenJDK 사용
FROM openjdk:17-jdk-slim

# 애플리케이션 디렉토리 생성
WORKDIR /app

# JAR 파일을 컨테이너의 /app 디렉토리로 복사
COPY build/libs/Gathering-0.0.1-SNAPSHOT.jar app.jar

# entrypoint.sh 스크립트를 컨테이너로 복사
COPY entrypoint.sh /app/entrypoint.sh

# 스크립트의 형식을 Unix로 변환 (필요한 경우)
RUN apt-get update && apt-get install -y dos2unix && dos2unix /app/entrypoint.sh

# 스크립트에 실행 권한 부여
RUN chmod +x /app/entrypoint.sh

# 애플리케이션 실행을 entrypoint.sh로 설정
ENTRYPOINT ["/app/entrypoint.sh"]