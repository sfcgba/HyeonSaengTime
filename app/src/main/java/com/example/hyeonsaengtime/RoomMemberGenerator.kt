package com.example.hyeonsaengtime

object RoomMemberGenerator {
    private val dummyNames = listOf(
        "민준",
        "서연",
        "도윤",
        "하린",
        "지호",
        "유나",
        "시우",
        "나은"
    )

    fun stableSlot(roomName: String, nickname: String): Int {
        return stableIndex("${roomName.trim()}:${nickname.trim()}", HyeonSaengRules.ROOM_MEMBER_COUNT) + 1
    }

    fun generate(
        dateKey: String,
        mySlot: Int,
        myNickname: String,
        isAnonymous: Boolean,
        myMillis: Long
    ): List<RoomMember> {
        val safeMySlot = mySlot.coerceIn(1, HyeonSaengRules.ROOM_MEMBER_COUNT)
        return (1..HyeonSaengRules.ROOM_MEMBER_COUNT).map { slot ->
            val isMe = slot == safeMySlot
            RoomMember(
                slot = slot,
                displayName = displayName(
                    slot = slot,
                    myNickname = myNickname,
                    isAnonymous = isAnonymous,
                    isMe = isMe
                ),
                hyeonsaengMillis = if (isMe) myMillis else dummyMillis(dateKey, slot),
                isMe = isMe
            )
        }
    }

    private fun displayName(
        slot: Int,
        myNickname: String,
        isAnonymous: Boolean,
        isMe: Boolean
    ): String {
        if (isAnonymous) {
            return if (isMe) "익명 $slot (나)" else "익명 $slot"
        }

        return if (isMe) {
            "$myNickname (나)"
        } else {
            dummyNames[slot - 1]
        }
    }

    private fun dummyMillis(dateKey: String, slot: Int): Long {
        val minutes = 7L * 60L + stableIndex("$dateKey:$slot", 14 * 60 + 1)
        return minutes * 60L * 1000L
    }

    private fun stableIndex(seed: String, size: Int): Int {
        var hash = 1125899906842597L
        seed.forEach { char ->
            hash = 31L * hash + char.code
        }
        return ((hash ushr 1) % size).toInt()
    }
}
