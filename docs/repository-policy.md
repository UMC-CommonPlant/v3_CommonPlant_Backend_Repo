# Repository Policy

## Branch 역할

### develop

개발 완료된 기능을 통합하고 개발 환경에 배포하기 위한 브랜치이다.

- feature 브랜치는 develop을 대상으로 Pull Request를 생성한다.
- 병합 방식은 Squash Merge를 사용한다.
- 직접 Push를 허용하지 않는다.
- Required Check와 최소 1명의 승인이 필요하다.

### main

운영 릴리스가 가능한 상태를 관리하는 브랜치이다.

- develop 브랜치에서만 Release Pull Request를 생성한다.
- 병합 방식은 Merge Commit을 사용한다.
- 직접 Push를 허용하지 않는다.
- Required Check와 최소 1명의 승인이 필요하다.
- main 병합은 이미지 재빌드가 아니라 검증된 이미지의 승격 기준으로 사용한다.

## Merge 전략

| Source | Target | Merge 방식 |
|---|---|---|
| feature/* | develop | Squash Merge |
| fix/* | develop | Squash Merge |
| develop | main | Merge Commit |

## Commit 및 PR 제목

PR 제목은 Conventional Commit 형식을 따른다.

- feat: 기능 추가
- fix: 오류 수정
- test: 테스트 추가 또는 변경
- refactor: 기능 변경 없는 구조 개선
- chore: 빌드 및 설정 변경
- docs: 문서 변경