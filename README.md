# HyeonSaengTime

## 이번 작업: 로컬 방 시뮬레이션 정산 보강

이번 브랜치에서는 로컬 방 시뮬레이션을 하루 하나의 방 미션, XP 상한, 개인/방 timezone 분리 기준으로 정리했습니다.

## 변경 내용

- 개인/방 timezone 분리
  - 개인 timezone은 `personal_time_zone_id`에 최초 1회 저장하고 계속 사용합니다.
  - 방 timezone은 방 생성 시 `room_host_time_zone_id`에 저장하고 방 미션 기준으로 고정합니다.
  - 개인 기록은 `total_yyyyMMdd`, 방 기준 기록은 `room_total_yyyyMMdd`에 따로 누적합니다.

- 하루 하나 방 미션
  - 방 미션 후보 4개 중 하루에 하나만 선택합니다.
  - 선택된 미션은 `room_mission_yyyyMMdd`에 저장해 같은 날짜에 바뀌지 않게 합니다.
  - 방 미션 정산 날짜는 방 timezone 기준 전날입니다.

- XP/레벨 보정
  - `room_xp`는 실제 저장 XP이며 0-500 범위로 유지합니다.
  - 선택된 미션 성공은 +20 XP, 미달성은 -10 XP입니다.
  - Lv5 100/100 상태에서 실패하면 실제 저장 XP가 490으로 내려갑니다.

- 방 생성/상태 읽기
  - 방 생성 화면의 초기값은 `방 1`, `나`입니다.
  - 빈 방 이름이나 닉네임은 저장하지 않습니다.
  - 방 상태는 `NotCreated`, `Created`, `Invalid`로 구분합니다.
  - 잘못된 방 상태는 자동 삭제하거나 기본값으로 조용히 복구하지 않습니다.

- 방 화면 표시
  - 상단의 내 현생시간은 개인 timezone 기준으로 표시합니다.
  - 개인 timezone과 방 timezone이 다를 때만 방 기준 현생시간을 작게 함께 표시합니다.
  - 오늘 깨야 하는 방 미션을 먼저 보여주고, 어제 미션은 정산 결과로만 짧게 보여줍니다.

## 저장 키

- `personal_time_zone_id`: 개인 날짜 기준 timezone
- `room_created`: 방 생성 여부
- `room_name`: 방 이름
- `room_nickname`: 내 닉네임
- `room_anonymous`: 익명 방 여부
- `room_level`: 방 레벨
- `room_xp`: 방 경험치
- `room_last_settled_date`: 마지막 정산 날짜
- `room_my_slot`: 내 고정 슬롯
- `room_host_time_zone_id`: 방 날짜 기준 timezone
- `room_mission_yyyyMMdd`: 날짜별 선택 미션 ID
- `room_total_yyyyMMdd`: 방 timezone 기준 날짜별 내 현생시간

## 테스트

추가/수정한 테스트:

- 개인 timezone 최초 저장과 유지 테스트
- 개인/방 timezone 기준 누적 저장 테스트
- 방 생성 입력 검증과 Invalid 상태 테스트
- 하루 하나 미션 선택/중복 정산 방지 테스트
- XP 0-500 상한과 Lv5 실패 감소 테스트

실행 명령:

```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" sh ./gradlew testDebugUnitTest
```
