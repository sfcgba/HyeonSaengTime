

## 변경사항 

TimeUtils.kt 생성 : imeUtils는 날짜별로 시간을 쪼개는 순수 계산 로직만을 담당. 이 로직을 ScreenReceiver나 TrackingService와 같은 컴포넌트에 직접 넣지 않고 분리하여 효율적으로 관리, 추후에 시간 분할 로직을 수정하거나, 날짜 계산 방식을 바꾸어야 할 때, 여러 곳을 뒤질 필요 없이 TimeUtils.kt 파일 하나만 수정하면 됨.

ScreenReceiver.kt 변경 : SCREEN_OFF와 USER_PRESENT이벤트를 수신하여 사용자가 폰을 안 보는 시간을 정확히 캐치하도록 구현. 정산(Settlement) 기준을 USER_PRESENT로 단일화

HomeScreen 변경 : LaunchedEffect와 delay를 활용해 불필요한 시스템 리소스 낭비 없이 UI를 부드럽게 갱신하도록 수정. 

안드로이드의 기본 저장소인 SharedPreferences를 활용해, active_lock_start_millis (측정 시작 시간)와 total_yyyy-MM-dd (날짜별 누적 시간)를 안전하고 빠르게 기록하고 꺼내 쓰도록 데이터베이스 구조 구축.


## adb / logcat 시나리오 테스트 결과

자정 분할 정산 로그 / 11시 58 ~ 12 시 1분 까지 자정이 넘어가는 시간을 사이에 두고 폰을 껐다 켠 경우(잠금화면은 설정 안함)

2026-06-07 08:56:36.436  8747-8747  HyeonsaengReceiver      com.example.hyeonsaengtime           D  [🧪 SCREEN_OFF] 잠금 상태 진입 - 전역 액티브 세션 기록 시작
2026-06-07 09:02:33.039  8747-8747  HyeonsaengReceiver      com.example.hyeonsaengtime           D  [🧪 SCREEN_ON] 잠금화면 없음 감지! 즉시 최종 세션 정산
2026-06-07 09:02:33.053  8747-8747  HyeonsaengReceiver      com.example.hyeonsaengtime           D    └ 📅 [자정 분할 정산] 날짜: 20260606 | 할당된 시간: 203초
2026-06-07 09:02:33.053  8747-8747  HyeonsaengReceiver      com.example.hyeonsaengtime           D    └ 📅 [자정 분할 정산] 날짜: 20260607 | 할당된 시간: 153초
2026-06-07 09:02:33.054  8747-8747  HyeonsaengReceiver      com.example.hyeonsaengtime           D    └ ▶ [최종 정산 완료] 총 356초 처리됨

SCREEN_ON(화면 켜짐)과 USER_PRESENT(잠금 해제 완료) 차이 로그 (잠금화면이 있는 상황에서 잠금화면을 해제하지 않으면 현생시간 안끊기고 누적)

2026-06-07 09:20:50.793 11880-11880 HyeonsaengReceiver      com.example.hyeonsaengtime           D  [🧪 SCREEN_OFF] 잠금 상태 진입 - 전역 액티브 세션 기록 시작
2026-06-07 09:22:13.782 11880-11880 HyeonsaengReceiver      com.example.hyeonsaengtime           D  [🧪 SCREEN_ON] 잠금화면 노출 상태 (정산 유예)
2026-06-07 09:22:29.181 11880-11880 HyeonsaengReceiver      com.example.hyeonsaengtime           D  [🧪 USER_PRESENT] 실제 사용자의 잠금 해제 완료 - 최종 세션 정산
2026-06-07 09:22:29.183 11880-11880 HyeonsaengReceiver      com.example.hyeonsaengtime           D    └ 📅 [자정 분할 정산] 날짜: 20260607 | 할당된 시간: 98초
2026-06-07 09:22:29.209 11880-11880 HyeonsaengReceiver      com.example.hyeonsaengtime           D    └ ▶ [최종 정산 완료] 총 98초 처리됨

잠금화면이 있는 상황에서 자정 분할 실행

2026-06-08 08:58:06.643 11880-11880 HyeonsaengReceiver      com.example.hyeonsaengtime           D  [🧪 SCREEN_OFF] 잠금 상태 진입 - 전역 액티브 세션 기록 시작
2026-06-08 09:01:29.283 11880-11880 HyeonsaengReceiver      com.example.hyeonsaengtime           D  [🧪 SCREEN_ON] 잠금화면 노출 상태 (정산 유예)
2026-06-08 09:01:41.159 11880-11880 HyeonsaengReceiver      com.example.hyeonsaengtime           D  [🧪 USER_PRESENT] 실제 사용자의 잠금 해제 완료 - 최종 세션 정산
2026-06-08 09:01:41.160 11880-11880 HyeonsaengReceiver      com.example.hyeonsaengtime           D    └ 📅 [자정 분할 정산] 날짜: 20260607 | 할당된 시간: 113초
2026-06-08 09:01:41.160 11880-11880 HyeonsaengReceiver      com.example.hyeonsaengtime           D    └ 📅 [자정 분할 정산] 날짜: 20260608 | 할당된 시간: 101초
2026-06-08 09:01:41.164 11880-11880 HyeonsaengReceiver      com.example.hyeonsaengtime           D    └ ▶ [최종 정산 완료] 총 214초 처리됨
