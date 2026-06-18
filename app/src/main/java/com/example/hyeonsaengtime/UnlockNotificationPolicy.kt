package com.example.hyeonsaengtime

object UnlockNotificationPolicy {
    const val CHANNEL_ID = "unlock_result_channel"
    const val CHANNEL_NAME = "잠금해제 후 안내"
    const val NOTIFICATION_ID = 2
    const val TITLE = "현생시간을 기록했어요"
    const val TEXT = "쌓인 시간은 결과에서 확인할 수 있어요"

    fun shouldNotify(
        result: TrackingSessionResult,
        extraNotificationsEnabled: Boolean
    ): Boolean {
        return extraNotificationsEnabled && result.update == TrackingSessionUpdate.FINALIZED
    }
}
