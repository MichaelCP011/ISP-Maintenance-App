package com.example.isp_icon.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [LokasiEntity::class, PersonilEntity::class, PertanyaanEntity::class, HeaderOptionEntity::class, InspectionStatusEntity::class, MasterSiteEntity::class], // <--- Tambah HeaderOptionEntity
    version = 4, // <--- NAIKKAN VERSI JADI 2 (PENTING!)
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "isp_app_database"
                )
                    .fallbackToDestructiveMigration() // Reset DB jika struktur berubah
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}