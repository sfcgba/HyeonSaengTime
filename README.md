# HyeonSaengTime

## 구현 메모

- 위치: `ScreenReceiver.kt`, `UsageSessionCalculator.kt`
- 잠금 기준: none은 `SCREEN_ON`에서 종료, swipe/PIN은 `USER_PRESENT`에서 종료
- 자정 처리: 잠금 시작 시점부터 다음 자정 또는 해제 시점까지 날짜별로 나눠 저장
- 이유: 자정을 넘긴 잠금 시간이 한 날짜에 몰리지 않게 하기 위함
- 다음 작업: 알림/위젯용 `getTodayLiveMillis(prefs, now)` 같은 실시간 계산 필요
- `UsageSessionCalculator.kt`로 따로 둔것은 나중에 알림/위젯등 쓰기 편하게 하기위해서 `ScreenReceiver.kt`와 분리가 필요하다 생각했음.
- 테스트 파일 하나 넣습니다.
