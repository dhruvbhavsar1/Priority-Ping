package com.example.priorityping.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.priorityping.model.PriorityContactEntity

@Database(
    entities = [PriorityContactEntity::class],
    version = 2
)
@TypeConverters(PriorityConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun priorityContactDao(): PriorityContactDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE priority_contacts ADD COLUMN addedAt INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE priority_contacts ADD COLUMN isActive INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE priority_contacts ADD COLUMN label TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "priority_ping_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
