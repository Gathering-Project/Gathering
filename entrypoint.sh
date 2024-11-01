##!/bin/bash
## S3에서 .env 파일 다운로드
#aws s3 cp s3://wearemeetnow/wemeetnowenv/.env /app/.env
#
## .env 파일 로드
#export $(cat /app/.env | xargs)
#
## 애플리케이션 시작
#exec "$@"
