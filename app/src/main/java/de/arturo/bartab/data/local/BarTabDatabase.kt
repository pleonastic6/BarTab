package de.arturo.bartab.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [CategoryEntity::class, ProductEntity::class, SaleEntity::class, SaleItemEntity::class],
    version = 4,
    exportSchema = false,
)
abstract class BarTabDatabase : RoomDatabase() {
    abstract fun barTabDao(): BarTabDao

    companion object {
        @Volatile
        private var INSTANCE: BarTabDatabase? = null

        fun getInstance(context: Context): BarTabDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    BarTabDatabase::class.java,
                    "bartab.db",
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
