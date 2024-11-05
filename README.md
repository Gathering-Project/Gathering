
WE MEET NOW 
=============

<br>

## 📢 프로젝트 소개

### **우리 지금 만나 : WE MEET NOW** 
**현대 사회의 개인주의 속에서, 취미를 함께할 사람들과 소모임을 쉽게 만들고 참여할 수 있는 플랫폼입니다.** <br>
웹 기반으로 다양한 활동을 함께할 사람들과 자유롭게 소모임을 구성할 수 있으며, 
안전하고 간편하게 소모임을 생성하고 활발한 커뮤니티를 형성할 수 있도록 직관적인 사용자 경험과 신뢰 기반의 환경을 제공합니다.

<br>

## 🛠️ 기술 스택
### Backend
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=Spring&logoColor=white)
![Spring](https://img.shields.io/badge/Spring%20JPA-6DB33F?style=for-the-badge&logo=Spring&logoColor=white)
![Spring](https://img.shields.io/badge/springsecurity-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![MySQL](https://img.shields.io/badge/mysql-4479A1.svg?style=for-the-badge&logo=mysql&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)
![Jenkins](	https://img.shields.io/badge/Jenkins-D24939?style=for-the-badge&logo=Jenkins&logoColor=white)
![Kafka](https://img.shields.io/badge/Kafka-231F20?style=for-the-badge&logo=Apache-Kafka&logoColor=white)
![Redis](https://img.shields.io/badge/redis-%23DD0031.svg?style=for-the-badge&logo=redis&logoColor=white)
![Redisson](https://img.shields.io/badge/Redisson-FFA500?style=for-the-badge)
![Redis Rate Limiter](https://img.shields.io/badge/Redis%20Rate%20Limiter-DC382D?style=for-the-badge&logo=Redis&logoColor=white)



### DevOps
![EC2](https://img.shields.io/badge/amazonec2-FF9900?style=for-the-badge&logo=amazonec2&logoColor=white)
![AWS S3](https://img.shields.io/badge/AWS%20S3-%23FF9900.svg?style=for-the-badge&logo=amazon-aws&logoColor=white)
![Docker](https://img.shields.io/badge/docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![AmazonRds](https://img.shields.io/badge/amazonrds-527FFF?style=for-the-badge&logo=amazonrds&logoColor=white)
![ECR](https://img.shields.io/badge/Amazon%20ECR-FF9900?style=for-the-badge&logo=Amazon-AWS&logoColor=white)
![JUnit5](https://img.shields.io/badge/JUnit5-FB4F14?style=for-the-badge&logo=JUnit5&logoColor=white)

### Tools
![GitHub](https://img.shields.io/badge/github-%23121011.svg?style=for-the-badge&logo=github&logoColor=white)
![Git](https://img.shields.io/badge/git-F05032?style=for-the-badge&logo=git&logoColor=white)
![IntelliJ IDEA](https://img.shields.io/badge/IntelliJIDEA-000000.svg?style=for-the-badge&logo=intellij-idea&logoColor=white)
![Jmeter](https://img.shields.io/badge/apachejmeter-D22128?style=for-the-badge&logo=apachejmeter&logoColor=white)
![Notion](https://img.shields.io/badge/Notion-%23000000.svg?style=for-the-badge&logo=notion&logoColor=white)
![Slack](https://img.shields.io/badge/Slack-4A154B?style=for-the-badge&logo=slack&logoColor=white)

[//]: # ([![Elasticsearch]&#40;https://img.shields.io/badge/elasticsearch-005571.svg?style=for-the-badge&logo=elasticsearch&logoColor=white&#41;]&#40;https://www.elastic.co/elasticsearch/&#41;)
[//]: # ([![Kibana]&#40;https://img.shields.io/badge/kibana-005571.svg?style=for-the-badge&logo=kibana&logoColor=white&#41;]&#40;https://www.elastic.co/kibana/&#41;)
[//]: # ([![Grafana]&#40;https://img.shields.io/badge/grafana-F46800.svg?style=for-the-badge&logo=grafana&logoColor=white&#41;]&#40;https://grafana.com/&#41;)
[//]: # (![Prometheus]&#40;https://img.shields.io/badge/prometheus-E6522C.svg?style=for-the-badge&logo=prometheus&logoColor=white&#41;)

<br>

## 🗯️ 주요 기능

* **안전한 사용자 인증**

    - [x] JWT와 Spring Security를 통해 사용자 인증 및 권한 관리를 제공하여 보안을 강화
    - [x] OAuth 2.0을 활용해 소셜 계정을 통한 간편 로그인 기능 제공

* **소모임과 이벤트 관리**

    - [x] 사용자 주도로 소모임을 생성하고, 필요한 정보를 쉽게 수정 및 관리할 수 있도록 구성
    - [x] 소모임 참가 신청과 관리자의 승인/거절 시스템을 제공하여 효율적인 참여 관리 지원
    - [x] 실시간 알림을 통해 신청 및 승인이 신속히 처리되며, 다양한 이벤트를 생성하고 구성원들이 쉽게 참여할 수 있는 환경 제공

* **매칭 서비스**

    - [x] 공통의 관심사나 위치를 기반으로 사용자 간 자동 매칭을 통해 소모임과 이벤트에 쉽게 참여할 기회 제공
    - [x] **Kafka**를 통한 비동기 메시징으로 안정적인 매칭 서비스 구현
  
* **동시성 제한 및 캐싱 관리**

    - [x] Redis와 Redisson 기반의 분산 락을 이용해 동시 참가 제한을 관리하여 신뢰성 높은 이벤트 참여 환경 보장
    - [x] Redis 캐싱을 통해 목록 조회 및 데이터 로딩 속도 최적화
    - [x] Redis Rate Limiter를 통해 사용자 요청 속도를 제어하고, 서비스 안정성 유지

* **실시간 알림 시스템**

    - [x] 참가 신청 승인, 새로운 이벤트 등 주요 활동에 대해 kafka를 활용한 실시간 알림 제공
    - [x] 사용자 맞춤 알림으로 중요한 소식을 놓치지 않고 확인 가능

* **검색 기능 및 인덱싱 최적화**

    - [x] 소모임 제목과 지역을 기준으로 소모임을 신속하게 검색할 수 있는 기능 제공
    - [x] 효율적인 인덱싱을 통해 검색 속도 향상, 대규모 데이터에서도 빠르게 결과 반환


[//]: # (* **모니터링 및 통계**)
[//]: # (    - [x] Grafana와 Prometheus를 통해 실시간 모니터링과 통계 제공)
[//]: # (    - [x] 통계를 활용해 소모임 운영 효율성을 높이고 개선 가능)

<br>

## 📑 API 명세서
<details>
  <summary><span style="font-size:1.2em"><strong>유저</strong></span></summary>

![erd.png](/assets/유저api1.png)
![erd.png](/assets/유저api2.png)

</details>

<details>
  <summary><span style="font-size:1.2em"><strong>멤버</strong></span></summary>

![erd.png](/assets/멤버api.png)
![erd.png](/assets/멤버api2.png)

</details>

<details>
  <summary><span style="font-size:1.2em"><strong>소모임</strong></span></summary>

![erd.png](/assets/소모임api1.png)
![erd.png](/assets/소모임api2.png)


</details>

<details>
  <summary><span style="font-size:1.2em"><strong>소모임 참여</strong></span></summary>

![erd.png](/assets/소모임참여api.png)

</details>

<details>
  <summary><span style="font-size:1.2em"><strong>이벤트</strong></span></summary>

![erd.png](/assets/이벤트api1.png)
![erd.png](/assets/이벤트api2.png)

</details>

<details>
  <summary><span style="font-size:1.2em"><strong>댓글</strong></span></summary>

![erd.png](/assets/댓글api.png)

</details>

<details>
  <summary><span style="font-size:1.2em"><strong>첨부파일</strong></span></summary>

![erd.png](/assets/첨부파일api.png)

</details>

<br>

## 🎨 와이어프레임
![와이어프레임](/assets/1105.png)

<br>

## 📊 ERD
![erd](/assets/ERD1105.png)

<br>

## 🖥️ 인프라 설계도
![infra](/assets/infra1105.png)

<br>

## 🔫 트러블 슈팅
## [WIKI 바로가기](https://github.com/Gathering-Project/Gathering/wiki)

<br>
<br>

## 💪 TEAM 같이의 가치
<table>
  <thead>
    <tr>
      <th align="center">팀원</th>
      <th align="center">포지션</th>
      <th align="center">담당</th>
      <th align="center">GitHub</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td align="center">
        <img src="/assets/profile5.png" width="100px;" alt="Profile Image"/><br/>
        <sub><b>김도균</b></sub>
      <td align="center">👑 Leader</td>
      <td align="left">
        - User, Auth 도메인 백엔드 개발<br/>
        - Spring Security를 통한 유저 회원가입/로그인/탈퇴 개발<br/>
        - Kafka를 통한 서비스간 비동기 통신 환경 구축<br/>
        - 필터링을 통한 1:1 유저 매칭 서비스 개발
      </td>
      <td align="center"><a href="https://github.com/gyun97">GitHub</a></td>
    </tr>
    <tr>
      <td align="center">
        <img src="/assets/profile_yuhwa.jpg" width="100px;" alt="Profile Image"/><br/>
        <sub><b>나유화</b></sub>
      <td align="center">👑 Sub-Leader</td>
      <td align="left">
        - 이벤트 도메인 백엔드 개발<br/>
        - OAuth 2.0 연동<br/>
        - Redisson 동시성 제어 처리<br/>
        - Redis master-slave 구조 및 sentinel 구축
      </td>
      <td align="center"><a href="https://github.com/fargoe">GitHub</a></td>
    </tr>
    <tr>
      <td align="center">
        <img src="/assets/profile1.jpeg" width="100px;" alt="Profile Image"/><br/>
        <sub><b>김민주</b></sub>
      </td>
      <td align="center">🫅🏻 Member</td>
      <td align="left">
        - 프로필 이미지 첨부파일 S3 <br/>
        - 목록 캐싱 및 Redis Rate Limiter <br/>
        - CI/CD(Jenkins) 파이프라인 구성<br/>
        - AWS Infra(EC2, S3, RDS, ECR) 구성 및 배포
      </td>
      <td align="center"><a href="https://github.com/wanduek">GitHub</a></td>
    </tr>
    <tr>
      <td align="center">
        <img src="/assets/profile2.JPG" width="100px;" alt="Profile Image"/><br/>
        <sub><b>장민경</b></sub><td align="center">🫅🏻 Member</td>
      <td align="left">
        - 댓글 도메인 백엔드 개발<br/>
        - 인덱싱을 통한 검색 최적화 적용
      </td>
      <td align="center"><a href="https://github.com/Minkyeongweb">GitHub</a></td>
    </tr>
    <tr>
      <td align="center">
        <img src="/assets/profile4.jpg" width="100px;" alt="Profile Image"/><br/>
        <sub><b>이시우</b></sub><td align="center">🫅🏻 Member</td>
      <td align="left">
        - 멤버 도메인 백엔드 개발 <br/>
        - Kafka를 통한 실시간 알림 연동
      </td>
      <td align="center"><a href="https://github.com/lsy8467">GitHub</a></td>
    </tr>
    <tr>
      <td align="center">
        <img src="/assets/profile3.png" width="100px;" alt="Profile Image"/><br/>
        <sub><b>박용준</b></sub><td align="center">🫅🏻 Member</td>
      <td align="left">
        - 소모임 도메인 백엔드 개발
      </td>
      <td align="center"><a href="https://github.com/Gorokgorokgo">GitHub</a></td>
    </tr>
  </tbody>
</table>

<br>
