# Gathering

## 개요 
- 유저, 소모임, 이벤트, 댓글, 채팅 등 OtterMeet의 핵심 기능을 관리하는 메인 백엔드 저장소입니다.

<br>

## 주요 기능

* **안전한 사용자 인증**

    - [x] JWT와 Spring Security를 통해 사용자 인증 및 권한 관리를 제공하여 보안을 강화
    - [x] OAuth 2.0을 활용해 소셜 계정을 통한 간편 로그인 기능 제공

* **소모임과 이벤트 관리**

    - [x] 사용자 주도로 소모임을 생성하고, 필요한 정보를 쉽게 수정 및 관리할 수 있도록 구성
    - [x] 소모임 참가 신청과 관리자의 승인/거절 시스템을 제공하여 효율적인 참여 관리 지원
    - [x] 실시간 알림을 통해 신청 및 승인이 신속히 처리되며, 다양한 이벤트를 생성하고 구성원들이 쉽게 참여할 수 있는 환경 제공

* **매칭 서비스**

    - [x] 공통의 관심사나 위치를 기반으로 사용자 간 자동 매칭을 통해 소모임과 이벤트에 쉽게 참여할 기회 제공
    - [x] **RabbitMQ**를 통한 비동기 메시징으로 안정적인 매칭 서비스 구현
  
* **동시성 제한 및 캐싱 관리**

    - [x] Redis와 Redisson 기반의 분산 락을 이용해 동시 참가 제한을 관리하여 신뢰성 높은 이벤트 참여 환경 보장
    - [x] Redis 캐싱을 통해 목록 조회 및 데이터 로딩 속도 최적화
    - [x] Redis Rate Limiter를 통해 사용자 요청 속도를 제어하고, 서비스 안정성 유지

* **실시간 알림 시스템**

    - [x] 참가 신청 승인, 새로운 이벤트 등 주요 활동에 대해 RabbitMQ를 활용한 실시간 알림 제공
    - [x] 사용자 맞춤 알림으로 중요한 소식을 놓치지 않고 확인 가능

* **검색 기능 및 인덱싱 최적화**

    - [x] 소모임 제목과 지역을 기준으로 소모임을 신속하게 검색할 수 있는 기능 제공
    - [x] 효율적인 인덱싱을 통해 검색 속도 향상, 대규모 데이터에서도 빠르게 결과 반환

<br>

## 🗃저장소 모아보기

| **Repository**       | **Description**                                                                                     | **Technology Stack**              | **Visibility** |
|-----------------------|-----------------------------------------------------------------------------------------------------|------------------------------------|----------------|
| **[Gathering](https://github.com/Gathering-Project/Gathering)**    | 유저, 소모임, 이벤트, 댓글, 채팅 등 OtterMeet의 핵심 기능을 관리하는 메인 백엔드 저장소입니다.      | Java, Spring Boot, Redis, MySQL   | Public         |
| **[Matching-RabbitMQ](https://github.com/Gathering-Project/Matching-RabbitMQ)** | RabbitMQ를 활용한 1:1 매칭 시스템. 사용자 관심사 및 위치 기반 매칭 처리.                             | Java, RabbitMQ                    | Public         |
| **[frontend](https://github.com/Gathering-Project/frontend)**     | 결제 시스템과 Kakao/Naver OAuth 로그인 기능을 제공하는 프론트엔드 저장소입니다.                       | JavaScript, React, Toss Payments  | Public         |
| **[matching-kafka](https://github.com/Gathering-Project/matching-kafka)** | ~~Kafka를 활용한 1:1 매칭 시스템~~ (현재 참고용으로 보존 중).   | Java, Kafka | Public

<br>

## 🚀 OtterMeet WIKI 바로가기
[OtterMeet WIKI :)](https://github.com/Gathering-Project/Gathering/wiki)

<br>

## 🚀 프로젝트 Read Me 바로가기
[Read Me X)](https://github.com/Gathering-Project)


