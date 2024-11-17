#!/bin/bash

# S3에서 .env 파일 다운로드 (aws-cli가 설치되어 있어야 함)
aws s3 cp s3://your-bucket-name/.env /app/.env

# .env 파일에서 환경 변수 설정
export $(grep -v '^#' /app/.env | xargs)

# 애플리케이션 실행
exec java -jar /app/app.jar