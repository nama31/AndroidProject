package com.example.mytest.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room database root.
 *
 * Schema bumps go here — increment [version] and add a `Migration`. We
 * intentionally do not register a TypeConverter for `repeatDays`: the entity
 * already stores it as an `Int`, and the bitmask ↔ `Set<DayOfWeek>` conversion
 * is performed in the entity↔domain [AlarmMapper] for clean separation.
 */
@Database(
    entities = [AlarmEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun alarmDao(): AlarmDao

    companion object {
        const val DATABASE_NAME = "alarmx.db"
    }
}
