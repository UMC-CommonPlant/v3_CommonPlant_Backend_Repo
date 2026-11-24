# Repository Policy

## 배경

Kargo CI/CD를 적용하면서 develop merge는 dev에 자동 배포되고, main merge는 prod 승격으로 이어진다.
PR 하나가 운영 API를 바꿀 수 있게 되어 다음 문제가 생길 수 있다.

- 운영 중인 애플리케이션에 영향을 주는 변경이 충분한 검토 없이 main에 포함될 수 있다.
- dev는 자동 배포라 기능을 확인할 시간이 짧다. develop 기능을 충분히 검증하지 못한 채 prod로 올라갈 수 있다.

그래서 develop과 main의 merge 조건을 GitHub ruleset으로 강제하고, prod에는 dev 배포를 거친 develop 커밋만 올린다.

## 배포 흐름

| 단계 | 동작 | Workflow |
|---|---|---|
| feature → develop merge | develop 커밋 이미지(`sha-<커밋>`) 빌드, Kargo가 dev에 자동 배포 | Build Dev Image (`build-dev-image`, `deploy-dev`) |
| develop → main release merge | merge commit의 두 번째 부모(develop 커밋) 이미지를 prod로 승격 | Deploy Prod (`deploy-prod`) |
| 되돌리기 | Actions → Deploy Prod → Run workflow에 이전 develop 커밋 SHA 입력 | Deploy Prod |

이미지는 develop에서 한 번만 빌드한다. prod에는 dev에 배포된 이미지만 올라가며, `Deploy Prod`는 develop에 없는 커밋(hotfix 브랜치 등)을 거부한다.

## Branch 역할

### develop

개발 완료된 기능을 통합하고 개발 환경에 배포하기 위한 브랜치이다.

- feature 브랜치는 develop을 대상으로 Pull Request를 생성한다.
- 병합 방식은 Squash Merge를 사용한다.
- 직접 Push를 허용하지 않는다.
- Required Check `validate`(PR Check)와 최소 1명의 승인이 필요하다.
- merge 후 `deploy-dev`가 성공할 때까지 dev 배포를 확인한다.

### main

운영 릴리스가 가능한 상태를 관리하는 브랜치이다.

- develop 브랜치에서만 Release Pull Request를 생성한다.
- 병합 방식은 Merge Commit을 사용한다.
- 직접 Push를 허용하지 않는다.
- Required Check `validate`, `deploy-dev`와 최소 2명의 승인이 필요하다.
- 승인 후 새 커밋이 push되면 다시 승인을 받는다.
- main 병합은 이미지 재빌드가 아니라 검증된 이미지의 승격 기준으로 사용한다.

승인 인원은 2명이다. PR 작성자는 자기 PR을 승인할 수 없고, read 권한 사용자의 승인은 필수 승인 수에
포함되지 않는다. write 이상 권한자가 3명이므로 작성자를 제외한 전원이 승인하는 것과 같다.

## Release 절차

1. develop 최신 커밋의 `deploy-dev`가 성공했는지 확인하고, dev에서 기능을 검증한다.
2. develop → main Release PR을 만든다. PR 본문에 포함된 기능과 운영 영향(DB 변경, API 변경)을 적는다.
3. 2명의 승인과 `deploy-dev` check 성공 후 **Create a merge commit**으로 합친다.
4. `Deploy Prod`가 성공하고 Environments → prod에 기록되는지 확인한다.

Release PR을 연 뒤 develop에 새 커밋이 merge되면 PR head가 바뀐다. 이 경우 새 커밋의 `deploy-dev`가
끝날 때까지 merge가 막히고, 이전 승인도 무효가 된다. 바뀐 내용을 다시 검토하고 승인한다.

main → develop back-merge는 하지 않는다. main에는 develop 커밋과 내용이 없는 merge commit만 있으므로
맞출 필요가 없다. 운영 오류 수정도 develop에 먼저 merge해 dev를 거친 뒤 Release PR로 올린다.

## Merge 전략

| Source | Target | Merge 방식 |
|---|---|---|
| feature/* | develop | Squash Merge |
| fix/* | develop | Squash Merge |
| develop | main | Merge Commit |

## GitHub 설정

| 대상 | 설정 |
|---|---|
| ruleset `protect-develop` | PR 필수, 승인 1명, Squash만 허용, Required Check `validate`, 리뷰 대화 해결 필수, 우회 없음 |
| ruleset `protect-main` | PR 필수, 승인 2명, 마지막 push 이후 승인, Merge Commit만 허용, Required Check `validate`·`deploy-dev`, 리뷰 대화 해결 필수, 우회 없음 |
| Environment `dev` | `develop` 브랜치만 배포 |
| Environment `prod` | `main` 브랜치만 배포 |

## Commit 및 PR 제목

PR 제목은 Conventional Commit 형식을 따른다.

- feat: 기능 추가
- fix: 오류 수정
- test: 테스트 추가 또는 변경
- refactor: 기능 변경 없는 구조 개선
- chore: 빌드 및 설정 변경
- docs: 문서 변경