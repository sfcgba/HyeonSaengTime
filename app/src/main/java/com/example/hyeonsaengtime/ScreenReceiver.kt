package com.example.hyeonsaengtime

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log

class ScreenReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "HyeonsaengReceiver"
        private const val PREFS_NAME = "hyeonsaeng"
        private const val KEY_ACTIVE_LOCK_START = "active_lock_start_millis"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()

        when (intent.action) {
            Intent.ACTION_SCREEN_OFF -> {
                val existingLockStart = prefs.getLong(KEY_ACTIVE_LOCK_START, 0L)
                if (existingLockStart == 0L) {
                    Log.d(TAG, "[🧪 SCREEN_OFF] 잠금 상태 진입 - 전역 액티브 세션 기록 시작")
                    prefs.edit().putLong(KEY_ACTIVE_LOCK_START, now).apply()
                } else {
                    Log.d(TAG, "[🧪 SCREEN_OFF] 이미 진행 중인 잠금 세션 유지 (잠금 해제 없이 화면만 껐다 켬)")
                }
            }
            Intent.ACTION_SCREEN_ON -> {
                // 기기의 잠금화면 상태를 확인하는 매니저 호출
                val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

                if (keyguardManager.isKeyguardLocked) {
                    Log.d(TAG, "[🧪 SCREEN_ON] 잠금화면 노출 상태 (정산 유예)")
                } else {
                    // 잠금화면이 없거나 스와이프 버그가 있는 기기의 경우 즉시 정산!
                    Log.d(TAG, "[🧪 SCREEN_ON] 잠금화면 없음 감지! 즉시 최종 세션 정산")
                    doSettlement(prefs, now)
                }
            }
            Intent.ACTION_USER_PRESENT -> {
                Log.d(TAG, "[🧪 USER_PRESENT] 실제 사용자의 잠금 해제 완료 - 최종 세션 정산")
                doSettlement(prefs, now)
            }
        }
    }

    // 정산 로직을 따로 분리하여 재사용 (SCREEN_ON 또는 USER_PRESENT에서 호출)
    private fun doSettlement(prefs: SharedPreferences, now: Long) {
        val lockStart = prefs.getLong(KEY_ACTIVE_LOCK_START, 0L)

        if (lockStart > 0L) {
            // 시작 시간부터 현재 시간까지 날짜별로 쪼개기
            val splits = TimeUtils.splitTimeByDays(lockStart, now)
            val editor = prefs.edit()

            var totalElapsedForLog = 0L

            // 쪼개진 날짜별로 각각 저금통에 넣기
            splits.forEach { (dateStr, elapsed) ->
                val keyTodayTotal = "total_$dateStr"
                val accumulated = prefs.getLong(keyTodayTotal, 0L)
                editor.putLong(keyTodayTotal, accumulated + elapsed)
                totalElapsedForLog += elapsed

                // 분할 정산 로그 출력
                Log.d(TAG, "  └ 📅 [자정 분할 정산] 날짜: $dateStr | 할당된 시간: ${elapsed / 1000}초")
            }

            // 정산이 끝났으므로 세션 초기화
            editor.putLong(KEY_ACTIVE_LOCK_START, 0L).apply()
            Log.d(TAG, "  └ ▶ [최종 정산 완료] 총 ${totalElapsedForLog / 1000}초 처리됨")
        } else {
            Log.w(TAG, "  └ ⚠️ 잠금 시작 기록이 없어 정산을 스킵합니다.")
        }
    }
}