package com.example.mytest.data.db

import java.time.DayOfWeek

/**
 * Bidirectional conversion between `Set<DayOfWeek>` and a 7-bit packed Int.
 *
 * Encoding (matches §4.1):
 * - Mon = 1, Tue = 2, Wed = 4, Thu = 8, Fri = 16, Sat = 32, Sun = 64.
 *
 * Used by [AlarmMapper]; intentionally NOT exposed as a Room `@TypeConverter`
 * because the entity column itself is already `Int` — keeping the conversion
 * at the mapper boundary means the data layer can be unit-tested without
 * spinning up a Room database.
 */
object RepeatDaysConverters {

    /**
     * Encode [days] into a bitmask. Bits are assigned in
     * [DayOfWeek.value]-1 order (Monday → bit 0, Sunday → bit 6), so
     * `setOf(MONDAY)` → `1` and `setOf(SUNDAY)` → `64`.
     */
    fun toBitmask(days: Set<DayOfWeek>): Int {
        var mask = 0
        for (day in days) {
            mask = mask or (1 shl (day.value - 1))
        }
        return mask
    }

    /**
     * Decode [bitmask] back into a deterministic, ascending-by-day-of-week
     * set. Unknown high bits (>= bit 7) are ignored.
     */
    fun fromBitmask(bitmask: Int): Set<DayOfWeek> {
        if (bitmask == 0) return emptySet()
        val days = LinkedHashSet<DayOfWeek>(7)
        for (i in 0 until 7) {
            if (bitmask and (1 shl i) != 0) {
                days += DayOfWeek.of(i + 1)
            }
        }
        return days
    }
}
