#!/usr/bin/env bash
# Kargo(homelab)에서 이미지 태그에 해당하는 Freight의 Stage 반영을 확인하거나 요청한다.
#
#   kargo-deploy.sh wait <stage> <image-tag>      자동 승격되는 Stage(dev)에 반영될 때까지 기다린다.
#   kargo-deploy.sh promote <stage> <image-tag>   Stage(prod)에 Promote를 요청하고 끝날 때까지 기다린다.
#
# 필요한 환경변수: KARGO_API_ADDRESS, KARGO_API_TOKEN, KARGO_PROJECT
# 두 KARGO_API_* 변수가 모두 있으면 kargo CLI는 login 없이 그 token을 쓴다.
set -Eeuo pipefail
# $(...) 안의 명령이 실패해도 스크립트를 멈춘다. 없으면 권한·연결 오류를
# "Freight가 아직 없다"로 보고 timeout까지 기다린다.
shopt -s inherit_errexit

MODE="${1:?mode(wait|promote)가 필요합니다}"
STAGE="${2:?stage가 필요합니다}"
IMAGE_TAG="${3:?image tag가 필요합니다}"

: "${KARGO_API_ADDRESS:?KARGO_API_ADDRESS가 비어 있습니다}"
: "${KARGO_API_TOKEN:?KARGO_API_TOKEN이 비어 있습니다}"
: "${KARGO_PROJECT:?KARGO_PROJECT가 비어 있습니다}"

FREIGHT_TIMEOUT_SECONDS="${FREIGHT_TIMEOUT_SECONDS:-900}"
PROMOTION_TIMEOUT_SECONDS="${PROMOTION_TIMEOUT_SECONDS:-900}"
POLL_SECONDS=20

# Job Summary에 남길 결과. 실패 지점에서 fail()이 NOTE를 채운다.
FREIGHT=""
PROMOTION=""
PHASE=""
RESULT="❌ 실패"
NOTE=""

fail() {
  NOTE="$1"
  echo "::error::$1" >&2
}

# Actions 실행 화면의 Summary에 배포 결과를 남긴다. 성공·실패 모두 EXIT에서 한 번 쓴다.
write_summary() {
  [[ -n "${GITHUB_STEP_SUMMARY:-}" ]] || return 0
  if [[ "${RESULT}" == "❌ 실패" && -z "${NOTE}" ]]; then
    NOTE="실행 로그에서 오류를 확인하세요."
  fi
  {
    echo "## Kargo ${STAGE} 배포"
    echo
    echo "| 항목 | 값 |"
    echo "| --- | --- |"
    echo "| 결과 | ${RESULT} |"
    echo "| Stage | \`${STAGE}\` |"
    echo "| 이미지 | \`${IMAGE_TAG}\` |"
    echo "| Freight | ${FREIGHT:+\`${FREIGHT}\`} |"
    echo "| Promotion | ${PROMOTION:+\`${PROMOTION}\` (${PHASE})} |"
    echo "| 소요 시간 | ${SECONDS}초 |"
    echo "| Kargo | [${KARGO_PROJECT}](${KARGO_API_ADDRESS}/project/${KARGO_PROJECT}) |"
    if [[ -n "${NOTE}" ]]; then
      echo
      echo "> ${NOTE}"
    fi
  } >> "${GITHUB_STEP_SUMMARY}"
}
trap write_summary EXIT

# 출력이 한 건이면 객체, 여러 건이면 List라서 둘 다 배열로 맞춘다.
items() { jq -c '(.items // [.]) | map(select(. != null and .metadata != null))'; }

find_freight() {
  kargo get freight --project "${KARGO_PROJECT}" -o json \
    | items \
    | jq -r --arg tag "${IMAGE_TAG}" \
        'map(select(any(.images[]?; .tag == $tag))) | first | .metadata.name // empty'
}

freight_json() {
  kargo get freight --project "${KARGO_PROJECT}" --name "$1" -o json
}

# 해당 Freight로 만들어진 가장 최근 Promotion의 phase
latest_promotion_phase() {
  kargo get promotions --project "${KARGO_PROJECT}" --stage "${STAGE}" -o json \
    | items \
    | jq -r --arg f "$1" \
        'map(select(.spec.freight == $f)) | sort_by(.metadata.creationTimestamp) | last
         | if . == null then "" else "\(.metadata.name) \(.status.phase // "Pending")" end'
}

# 찾은 Freight 이름을 FREIGHT에 채운다.
wait_for_freight() {
  local deadline=$(( SECONDS + FREIGHT_TIMEOUT_SECONDS ))
  while (( SECONDS < deadline )); do
    FREIGHT="$(find_freight)"
    if [[ -n "${FREIGHT}" ]]; then
      return 0
    fi
    echo "Freight(${IMAGE_TAG})가 아직 없습니다. Warehouse 조회를 기다립니다."
    sleep "${POLL_SECONDS}"
  done
  fail "${FREIGHT_TIMEOUT_SECONDS}초 안에 ${IMAGE_TAG} Freight가 생기지 않았습니다. Warehouse와 GHCR 이미지를 확인하세요."
  return 1
}

wait_for_promotion() {
  local freight="$1" deadline=$(( SECONDS + PROMOTION_TIMEOUT_SECONDS )) result
  while (( SECONDS < deadline )); do
    result="$(latest_promotion_phase "${freight}")"
    if [[ -n "${result}" ]]; then
      read -r PROMOTION PHASE <<< "${result}"
      case "${PHASE}" in
        Succeeded)
          RESULT="✅ 성공"
          echo "Promotion ${PROMOTION} 성공 (${STAGE}, Freight ${freight})"
          return 0 ;;
        Failed|Errored|Aborted)
          fail "Promotion ${PROMOTION} 결과: ${PHASE}. Kargo UI에서 원인을 확인하세요."
          return 1 ;;
        *)
          echo "Promotion ${PROMOTION}: ${PHASE}" ;;
      esac
    else
      echo "${STAGE} Promotion이 아직 없습니다."
    fi
    sleep "${POLL_SECONDS}"
  done
  fail "${PROMOTION_TIMEOUT_SECONDS}초 안에 ${STAGE} Promotion이 끝나지 않았습니다."
  return 1
}

case "${MODE}" in
  wait)
    wait_for_freight
    echo "Freight: ${FREIGHT} (${IMAGE_TAG})"
    wait_for_promotion "${FREIGHT}"
    ;;

  promote)
    FREIGHT="$(find_freight)"
    if [[ -z "${FREIGHT}" ]]; then
      fail "${IMAGE_TAG} Freight가 없습니다. develop에서 빌드되어 dev에 배포된 커밋인지 확인하세요."
      exit 1
    fi
    echo "Freight: ${FREIGHT} (${IMAGE_TAG})"

    STATUS="$(freight_json "${FREIGHT}")"
    if jq -e --arg s "${STAGE}" '.status.currentlyIn[$s] != null' <<< "${STATUS}" >/dev/null; then
      RESULT="⏭️ 변경 없음"
      NOTE="이미 ${STAGE}에 배포된 Freight입니다. 새 Promotion을 만들지 않았습니다."
      echo "${NOTE}"
      exit 0
    fi
    # prod Stage는 dev를 통과한 Freight만 받는다. Kargo도 거절하지만 원인을 먼저 알린다.
    if ! jq -e '.status.verifiedIn.dev != null' <<< "${STATUS}" >/dev/null; then
      fail "${FREIGHT}는 dev에서 검증되지 않았습니다. dev 배포가 끝난 커밋만 ${STAGE}에 올릴 수 있습니다."
      exit 1
    fi

    kargo promote --project "${KARGO_PROJECT}" --freight "${FREIGHT}" --stage "${STAGE}"
    # --wait는 결과와 관계없이 끝나므로 phase를 직접 확인한다.
    wait_for_promotion "${FREIGHT}"
    ;;

  *)
    fail "알 수 없는 mode: ${MODE}"
    exit 1
    ;;
esac
