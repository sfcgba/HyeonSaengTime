# HyeonSaengTime



## 현재 추적 구조

- `TrackingService`
  - foreground service로 실행되며 `SCREEN_OFF`, `SCREEN_ON`, `USER_PRESENT` receiver를 동적 등록합니다.
  - `onStartCommand()`에서 `START_STICKY`를 반환합니다.
  - `START_STICKY`는 서비스 프로세스가 죽었을 때 시스템이 재시작을 시도하게 하는 best-effort 힌트입니다. 강제 종료, 사용자 중지, 재부팅, receiver 미등록 상태에서 놓친 screen event 복구를 보장하지 않습니다.

- `ScreenReceiver`
  - Android broadcast를 받는 진입점입니다.
  - 마지막 screen event를 prefs에 로그성 값으로 저장합니다.
  - 직접 total을 정산하지 않고 `TrackingSessionManager`에 위임합니다.

- `TrackingSessionManager`
  - 진행 중인 잠금 세션 상태와 prefs 저장을 관리합니다.
  - `SCREEN_OFF`에서 `active_lock_start_millis`를 저장합니다.
  - `SCREEN_ON`에서 keyguard가 잠겨 있지 않으면 세션을 종료합니다.
  - keyguard가 잠겨 있으면 `USER_PRESENT`까지 기다렸다가 세션을 종료합니다.
  - 세션 종료 시 `total_yyyyMMdd`에 날짜별 누적 시간을 저장하고 active session을 삭제합니다.


## 왜 이렇게 바꿨는가

- `ScreenReceiver`가 broadcast 수신과 시간 정산을 동시에 담당하면, 앱이 죽거나 이벤트가 누락되는 상황을 다루기 어려워집니다.
- receiver는 "받은 이벤트를 기록하고 넘기는 역할"로 좁히고, 세션 상태 변경은 `TrackingSessionManager`로 모았습니다.
-  `UsageSessionCalculator`는 context 나 prefs를 모르게 정말 순수하게 계산만 하게 역할을 나눠 새션 상태 변경은 위 클래스가 담당하게 했습니다.
- `active_lock_start_millis`를 prefs에 남겨두면 프로세스가 죽어도 진행 중이던 세션 시작 시각은 보존됩니다. 다만 이번 단계에서는 앱/서비스 재시작 시 stale session 자동 복구는 아직 넣지 않았습니다.

## 저장 키

- `active_lock_start_millis`: 현재 진행 중인 screen off 세션 시작 시각
- `total_yyyyMMdd`: 날짜별 누적 잠금 시간
- `last_screen_event_action`: 마지막으로 받은 screen event action
- `last_screen_event_at_millis`: 마지막으로 받은 screen event 시각

## 현재 한계

- `SCREEN_ON`, `SCREEN_OFF`는 manifest receiver로 안정적으로 받을 수 없고, 실행 중인 서비스에서 동적 receiver로 받아야 합니다.
- 서비스가 죽어 receiver가 등록되지 않은 동안 발생한 screen event는 놓칠 수 있습니다.
- `START_STICKY`는 대부분의 단순 kill 상황에서 도움이 될 수 있지만, 추적 정확성을 보장하는 장치는 아닙니다.
- 재부팅 후 자동 재개(`BOOT_COMPLETED`), Direct Boot, 앱 재시작 시 미완료 세션 복구는 다음 단계에서 별도로 설계해야 합니다.

## 테스트

```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" sh ./gradlew testDebugUnitTest
```

현재 로컬 기본 Java가 26이면 Gradle/Kotlin이 버전 문자열을 파싱하지 못해 실패할 수 있습니다. Android Studio 내장 JBR 21로 실행하면 unit test가 통과합니다.
