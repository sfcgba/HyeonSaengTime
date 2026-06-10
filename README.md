# HyeonSaengTime

## 이번 작업: 로컬 계산 코어 정리

이번 브랜치에서는 홈 화면에 섞여 있던 저장소 접근과 계산 로직을 분리했습니다. 화면은 값을 보여주는 역할에 집중하고, 날짜 키 생성, 오늘 진행률 계산, streak 계산, SharedPreferences 접근은 별도 클래스로 나눴습니다.

## 변경 내용

- 수면시간 차감 제거
  - 현생시간은 `total_yyyyMMdd`에 저장된 날짜별 누적 잠금 시간을 그대로 사용합니다.
  - `sleep_hours`는 더 이상 읽거나 쓰지 않습니다.

- 사용자 목표시간 설정 제거
  - 사용자가 하루 목표 시간을 직접 설정하지 않습니다.
  - `goal_hours`는 더 이상 읽거나 쓰지 않습니다.
  - streak 기준은 앱이 정한 고정값을 사용합니다.

- 고정 규칙 추가
  - `HyeonSaengRules`에서 앱 규칙을 관리합니다.
  - 현재 streak 기준은 16시간입니다.

- 날짜 키 생성 분리
  - `DateKeyFormatter`에서 `yyyyMMdd` 날짜 키를 생성합니다.
  - `total_yyyyMMdd`, `streak_last_date`의 날짜 기준을 한 곳으로 통일했습니다.

- 오늘 진행률 계산 분리
  - `TodayProgressCalculator`에서 오늘 현생시간과 streak 기준 진행률을 계산합니다.
  - 홈 화면은 이 계산 결과를 받아 표시합니다.

- streak 계산 분리
  - `StreakCalculator`에서 어제 현생시간 기준으로 streak 증가/초기화를 계산합니다.
  - 같은 날짜에 streak가 중복 계산되지 않도록 `streak_last_date`를 사용합니다.

- 로컬 저장소 래퍼 추가
  - `HyeonSaengLocalStore`에서 SharedPreferences 접근을 담당합니다.
  - 화면은 prefs 키를 직접 읽지 않고 store를 통해 오늘 진행 상태와 streak를 가져옵니다.

- 설정 화면 제거
  - 수면시간과 목표시간 설정이 모두 제거되면서 `SettingsScreen`도 삭제했습니다.
  - `MainActivity`는 현재 홈 화면만 표시합니다.

## 저장 키 변경

현재 사용하는 키:

- `total_yyyyMMdd`: 날짜별 누적 현생시간
- `streak_count`: 현재 연속 달성일
- `streak_last_date`: streak를 마지막으로 계산한 날짜
- `active_lock_start_millis`: 진행 중인 잠금 세션 시작 시각
- `last_screen_event_action`: 마지막 screen event action
- `last_screen_event_at_millis`: 마지막 screen event 시각

더 이상 사용하지 않는 키:

- `sleep_hours`
- `goal_hours`

## 테스트

추가/수정한 테스트:

- 날짜 키 생성 테스트
- 오늘 현생시간/streak 기준 진행률 계산 테스트
- streak 증가/초기화/중복 계산 방지 테스트
- `HyeonSaengLocalStore`의 오늘 진행 상태와 streak 갱신 테스트

실행 명령:

```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" sh ./gradlew testDebugUnitTest
```
