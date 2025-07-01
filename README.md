# FitPass
![fitpass](https://github.com/user-attachments/assets/369e2683-f202-4092-8579-f25d6c73f7ad)

## 팀 소개


---

## 프로젝트 소개

헬스장과 개인 트레이너, 회원을 연결하는 **통합 플랫폼**입니다.
사용자는 원하는 헬스장을 선택하고 **트레이너와 PT 이용권을 포인트로 예약** 할 수 있습니다.
PT 뿐만 아니라 1주일, 한달 등의 이용권도 포인트로 구매할 수 있습니다.
오프라인 헬스장의 PT 예약을 **더 유연하고 간편하게 바꾸는 서비스**입니다.

---

## 프로젝트 선정 배경
기존 헬스장 시스템은 한 번 등록하면 장기간 이용해야 하고, PT 등록 후 트레이너와 맞지 않더라도 **환불이 어렵다는 문제**가 있습니다. 이로 인해 사용자는 불편함을 감수하며 PT를 억지로 이어가야 하는 상황이 종종 발생합니다.
**Fitpass**는 이러한 문제를 해결하기 위해 만들어졌습니다.
- **PT를 1회 단위로 유연하게 이용**할 수 있고,
- **트레이너가 맞지 않더라도 환불이나 변경이 쉬운 구조**로 설계되어 사용자가 부담 없이 다양한 트레이너와 서비스를 경험할 수 있도록 돕습니다.

---

## 프로젝트의 전체적인 구조 (Architecture)

### AWS 인프라 아키텍처
(이미지 넣기)
```
┌──────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   GitHub Actions │    │     AWS EC2     │    │   AWS S3 CDN    │
│   (CI/CD)        │───▶│   (Application) │───▶│  (Image Store)  │
└──────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                         ┌─────────────────┐
                         │  MySQL + Redis  │
                         │   (Database)    │
                         └─────────────────┘
```

### System Architecture
Monolithic Architecture 모놀리식 아키텍처 기반 설계
DDD (Domain-Driven Design) 원칙에 따라 도메인별로 모듈을 분리하여 유지보수성과 확장성을 높였으며, Spring Security + JWT를 통해 안전한 인증 시스템을 구축했습니다.

### DDD 도메인 기반
```
src/main/java/org/example/fitpass/
├── common/                     # 공통 모듈
│   ├── jwt/                   # JWT 인증
│   ├── oauth2/                # OAuth2 소셜 로그인
│   ├── security/              # Spring Security 설정
│   ├── s3/                    # AWS S3 파일 업로드
│   ├── error/                 # 예외 처리
│   └── dto/                   # 공통 DTO
│
├── domain/                     # 비즈니스 도메인
│   ├── auth/                  # 인증 도메인
│   │   ├── controller/        # 인증 API
│   │   ├── service/           # 인증 비즈니스 로직
│   │   ├── repository/        # 인증 데이터 접근
│   │   ├── entity/            # 인증 엔티티
│   │   └── dto/               # 인증 DTO
│   │
│   ├── user/                  # 사용자 도메인
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   └── dto/
│   │
│   ├── gym/                   # 헬스장 도메인
│   ├── trainer/               # 트레이너 도메인
│   ├── reservation/           # 예약 도메인
│   ├── membership/            # 멤버십 도메인
│   ├── chat/                  # 채팅 도메인
│   ├── post/                  # 게시글 도메인
│   ├── review/                # 리뷰 도메인
│   └── ...                    # 기타 도메인
│
└── config/                     # 설정 클래스
    ├── SecurityConfig.java
    ├── RedisConfig.java
    ├── WebSocketConfig.java
    ├── SwaggerConfig.java
    └── DatabaseConfig.java
```

### 계층형 아키텍처
```
┌─────────────────────────────────────┐
│            Presentation Layer       │ ← Controller
├─────────────────────────────────────┤
│            Business Layer           │ ← Service
├─────────────────────────────────────┤  
│            Persistence Layer        │ ← Repository
├─────────────────────────────────────┤
│            Database Layer           │ ← MySQL, Redis
└─────────────────────────────────────┘
```
---

## 기술 스택

### Language & Framework
![Java 17](https://img.shields.io/badge/Java%2017-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Spring](https://img.shields.io/badge/Spring-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring%20Data%20JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![QueryDSL](https://img.shields.io/badge/QueryDSL-4479A1?style=for-the-badge&logo=querydsl&logoColor=white)

### Database & Cache
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![AWS RDS](https://img.shields.io/badge/AWS%20RDS-527FFF?style=for-the-badge&logo=amazon-rds&logoColor=white)

### Security & Authentication
![Spring Security](https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=spring-security&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=json-web-tokens&logoColor=white)

### Infrastructure & Cloud
![AWS](https://img.shields.io/badge/AWS-FF9900?style=for-the-badge&logo=amazon-aws&logoColor=white)
![AWS EC2](https://img.shields.io/badge/AWS%20EC2-FF9900?style=for-the-badge&logo=amazon-ec2&logoColor=white)
![AWS S3](https://img.shields.io/badge/AWS%20S3-569A31?style=for-the-badge&logo=amazon-s3&logoColor=white)

### DevOps & CI/CD
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub%20Actions-2088FF?style=for-the-badge&logo=github-actions&logoColor=white)

### Development Tools
![IntelliJ IDEA](https://img.shields.io/badge/IntelliJ%20IDEA-000000?style=for-the-badge&logo=intellij-idea&logoColor=white)
![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)
![Postman](https://img.shields.io/badge/Postman-FF6C37?style=for-the-badge&logo=postman&logoColor=white)

### Documentation & API
![Swagger](https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)

### Real-time & Communication
![WebSocket](https://img.shields.io/badge/WebSocket-010101?style=for-the-badge&logo=socket.io&logoColor=white)
![SSE](https://img.shields.io/badge/SSE-FF6B6B?style=for-the-badge&logo=server-sent-events&logoColor=white)

### Monitoring & Testing
![Prometheus](https://img.shields.io/badge/Prometheus-E6522C?style=for-the-badge&logo=prometheus&logoColor=white)
![Grafana](https://img.shields.io/badge/Grafana-F46800?style=for-the-badge&logo=grafana&logoColor=white)
![JMeter](https://img.shields.io/badge/JMeter-D22128?style=for-the-badge&logo=apache-jmeter&logoColor=white)
![K6](https://img.shields.io/badge/K6-7D64FF?style=for-the-badge&logo=k6&logoColor=white)

### Collaboration
![Slack](https://img.shields.io/badge/Slack-4A154B?style=for-the-badge&logo=slack&logoColor=white)
![Notion](https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=notion&logoColor=white)
![Figma](https://img.shields.io/badge/Figma-F24E1E?style=for-the-badge&logo=figma&logoColor=white)
![Canva](https://img.shields.io/badge/Canva-00C4CC?style=for-the-badge&logo=canva&logoColor=white)

---

## 프로젝트 주요 기능
(자세한 정리는 WIKI URL 여기에 정리해두었습니다.)

### 사용자 기능
JWT 기반 회원가입/로그인, OAuth2 소셜 로그인, 프로필 관리

### 예약 시스템
실시간 PT 예약, Redis 분산 락 기반 동시성 제어, 유연한 예약 관리

### 헬스장 & 트레이너, 이용권 관리
헬스장 등록/관리, 트레이너 프로필 관리, 멤버십 상품 판매

### 포인트 시스템
포인트 충전/사용/환불, 결제 시스템, 포인트 내역 관리

### 운동 목표 설정 기능
개인 운동 목표 설정, 일일 기록 관리, 체중 추적 및 진행률 분석

### 소통 및 커뮤니티
실시간 채팅, 헬스장별 커뮤니티, 게시글 작성 및 좋아요 기능

### 리뷰 및 평가
헬스장/트레이너 리뷰 작성, 평점 시스템, 리뷰 통계

### 검색 기능
통합 검색, 다양한 필터링, 인기 검색어 관리

### 알림 서비스
예약/멤버십 알림, 실시간 푸시 알림

###  좋아요 기능
헬스장, 게시글, 트레이너 등에 대한 좋아요 시스템

---
## 프로젝트 문서
### API 문서 확인
http://localhost:8080/swagger-ui/index.html


### ERD



### API 명세서

---

## 프로젝트 중점사항
### 개발 원칙
- 코드 품질 : google style 코드 컨벤션 유지
- 문서화 : Wiki 관리 및 트러블 슈팅 관리
- 테스트 커버리지 : 단위테스트 80% 이상 유지

### 브랜치 전략
- **Git Flow**: main(배포) ← dev(개발) ← feature(기능)
- **코드 리뷰**: 모든 PR은 2명 이상 승인 후 머지
- **자동화**: CI/CD 파이프라인을 통한 자동 배포

### 보안 정책  
- **인증 전략**: JWT 기반 Stateless 인증 구조
- **권한 관리**: 역할별 세분화된 접근 제어
- **소셜 연동**: 사용자 편의성을 위한 OAuth2 도입

### 권한 관리 체계
- **USER → OWNER 승급**: 사용자 신청 → 관리자 승인 방식
- **헬스장 등록**: OWNER 신청 → 관리자 승인 후 운영 가능
- **포인트 시스템**: 관리자 직접 관리 및 충전 승인
- **트레이너 관리**: OWNER가 직접 등록/관리 (관리자 승인 불필요)
- **이용권 관리**: OWNER가 자율적으로 상품 등록/관리

### 모니터링 체계
- **성능 모니터링**: Prometheus + Grafana 실시간 지표 수집
- **로그 관리**: 구조화된 로깅으로 디버깅 효율성 증대
- **알림 시스템**: 장애 발생시 즉시 알림

---
  
## CI/CD
GitHub Actions
Docker
AWS

---

## 기술적 의사 결정


## 트러블 슈팅

## 성능 테스트
