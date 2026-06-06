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
