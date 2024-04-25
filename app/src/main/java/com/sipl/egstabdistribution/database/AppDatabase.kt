package com.sipl.egstabdistribution.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sipl.egstabdistribution.database.dao.AreaDao
import com.sipl.egstabdistribution.database.dao.UserDao
import com.sipl.egstabdistribution.database.entity.AreaItem
import com.sipl.egstabdistribution.database.entity.User
import java.util.concurrent.Executors

@androidx.room.Database(entities = [AreaItem::class,User::class], version = 2,exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun areaDao(): AreaDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).addCallback(object : RoomDatabase.Callback(){
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                    }
                })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}