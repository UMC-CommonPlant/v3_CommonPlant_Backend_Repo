# v3_CommonPlant_Backend_Repo

요청하신 내용을 바탕으로 **커먼플랜트(Common Plant)** 프로젝트의 `README.md` 초안을 작성해 드립니다. 이 내용을 프로젝트 루트 폴더에 복사하여 사용하시고, 상황에 맞춰 세부 내용을 수정해 보세요.

-----

# 🌿 Common Plant (커먼플랜트) - Backend

> **식물을 사랑하는 사람들을 위한 반려 식물 관리 서비스**
>
> 초보 식물 집사부터 전문가까지, 모든 사용자가 식물을 더 쉽게 관리하고 성장 과정을 기록할 수 있도록 돕는 플랫폼입니다.

-----

## 🚀 1. 프로젝트 개요

- **서비스 명:** 커먼플랜트 (Common Plant)
- **주요 기능:**
  - `Place` : 함께할 장소를 만들고 친구를 초대해 
  - `Plant` : 식물의 정보와 물주기를 함께 관리할 수 있고 
  - `Memo` : 식물의 상태를 메모로 기록하여 공유할 수 있으며 
  - `Calendar` : 식물과 관련된 모든 일정을 하나의 캘린더에서 관리할 수 있어요!
  - `Information` : 식물 추천과 가이드북을 통해 필요한 정보를 확인할 수 있어요!

## 🛠 2. 기술 스택 (Tech Stack)

### Backend

- **Language:** Java 17
- **Framework:** Spring Boot 3.x
- **Build Tool:** Gradle

### Database & Storage

- **Database:** PostgreSQL
- **Storage:** Local File Storage (이미지 및 데이터 파일 관리)

### Infrastructure

- **Server:** AWS EC2
- **Deployment:** (예: GitHub Actions / Docker 등 - 추후 추가 가능)

-----

## 🏗 3. 시스템 아키텍처 (Architecture)

### 📊 Entity Relationship Diagram (ERD)

> 아래 영역에 ERD 이미지를 첨부하거나 링크를 연결해 주세요.

### 🔌 Server Architecture

- **Web Server:** AWS EC2 기반의 어플리케이션 서버 운영
- **Storage:** 로컬 파일 시스템을 활용한 프로필 및 식물 이미지 저장 관리

-----

## 📋 4. Git Convention & Rules

### 💬 Commit Message Format

`Type: Subject` 형식을 사용하며, 본문은 선택사항입니다.

| Type | Description |
| :--- | :--- |
| `feat` | 새로운 기능 추가 |
| `fix` | 버그 수정 |
| `docs` | 문서 수정 (README 등) |
| `style` | 코드 의미에 영향을 주지 않는 변경 (포맷팅, 세미콜론 누락 등) |
| `refactor` | 코드 리팩토링 |
| `test` | 테스트 코드 추가 및 수정 |
| `chore` | 빌드 업무, 패키지 매니저 설정 등 코드 외적인 변경 |

### 🌿 Branch Strategy

- `main`: 배포용 브랜치
- `develop`: 개발 통합 브랜치
- `feat/기능명`: 각 기능 개발 브랜치

-----

## 👥 5. Backend Team

| [이름/닉네임] | [이름/닉네임] | [이름/닉네임] |
| :---: | :---: | :---: |
|  |  |  |
| **Role:** Lead / API Design | **Role:** DB / Infrastructure | **Role:** Business Logic / QA |
| [@github\_id](https://github.com/) | [@github\_id](https://github.com/) | [@github\_id](https://github.com/) |

-----

## ⚙️ 6. 시작 가이드 (Getting Started)

### Prerequisites

- JDK 17
- PostgreSQL 14+

### Installation

```bash
# 레포지토리 클론
git clone https://github.com/your-repo/common-plant-backend.git

# 설정 파일 작성 (application.yml 설정 필요)
cd common-plant-backend/src/main/resources
cp application-example.yml application.yml

# 프로젝트 빌드 및 실행
./gradlew build
java -jar build/libs/common-plant-0.0.1-SNAPSHOT.jar
```