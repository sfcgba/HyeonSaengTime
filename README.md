# HyeonSaengTime

## 이번 작업: 결과/설정/알림 MVP 정리

결과 화면을 어제 기록 중심 카드 형태로 정리하고, 개인 설정과 부가 알림 설정을 로컬 MVP 범위로 추가했습니다.

## 변경 내용

- 결과 화면
  - 어제의 현생 시간, 연속 달성 상태, 개인 목표 달성 여부를 표시합니다.
  - 방이 있으면 어제 방 미션 달성 여부, 방 레벨 진행도, XP 정산 변화, 방 내 순위를 함께 표시합니다.
  - 방 내 순위 영역을 펼치면 멤버별 기록과 개인 목표 기준 단계 비교를 볼 수 있습니다.

- 설정 화면
  - 하루 목표, 닉네임, 익명 표시, 알림 on/off만 둡니다.
  - 하루 목표는 개인 progress와 앞으로의 streak 판정에만 반영합니다.
  - 닉네임/익명 설정은 사용자 설정에 저장하고, 기존 방이 있으면 방 표시 정보도 함께 갱신합니다.

- 알림
  - 측정 중 foreground 알림은 계속 유지합니다.
  - 설정의 알림 off는 잠금해제 직후 부가 알림만 끕니다.
  - 부가 알림 문구는 기록 안내 중심으로 유지합니다.

## 저장 키

- `daily_goal_hours`: 개인 하루 목표 시간
- `user_nickname`: 설정 화면의 닉네임
- `user_anonymous`: 설정 화면의 익명 표시 여부
- `extra_notifications_enabled`: 잠금해제 직후 부가 알림 여부

## 테스트

실행 명령:

```bash
JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' sh gradlew test
```
