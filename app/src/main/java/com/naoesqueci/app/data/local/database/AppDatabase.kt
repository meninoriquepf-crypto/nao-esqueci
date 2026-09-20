package com.naoesqueci.app.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.naoesqueci.app.data.local.database.dao.CheckStateDao
import com.naoesqueci.app.data.local.database.dao.NotificationEventDao
import com.naoesqueci.app.data.local.database.dao.TripDao
import com.naoesqueci.app.data.local.database.dao.TripItemDao
import com.naoesqueci.app.data.local.database.entity.CheckStateEntity
import com.naoesqueci.app.data.local.database.entity.NotificationEventEntity
import com.naoesqueci.app.data.local.database.entity.TripEntity
import com.naoesqueci.app.data.local.database.entity.TripItemEntity

@Database(
    entities = [
        TripEntity::class,
        TripItemEntity::class,
        CheckStateEntity::class,
        NotificationEventEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun tripItemDao(): TripItemDao
    abstract fun checkStateDao(): CheckStateDao
    abstract fun notificationEventDao(): NotificationEventDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nao_esqueci.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}