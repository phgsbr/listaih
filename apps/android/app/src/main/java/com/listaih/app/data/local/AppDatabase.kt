package com.listaih.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.listaih.app.data.local.dao.HouseholdDao
import com.listaih.app.data.local.dao.ListItemDao
import com.listaih.app.data.local.dao.PriceEntryDao
import com.listaih.app.data.local.dao.ProductDao
import com.listaih.app.data.local.dao.ShoppingListDao
import com.listaih.app.data.local.dao.SyncQueueDao
import com.listaih.app.data.local.dao.UserDao
import com.listaih.app.data.local.entity.HouseholdEntity
import com.listaih.app.data.local.entity.ListItemEntity
import com.listaih.app.data.local.entity.PriceEntryEntity
import com.listaih.app.data.local.entity.ProductEntity
import com.listaih.app.data.local.entity.ShoppingListEntity
import com.listaih.app.data.local.entity.SyncQueueEntity
import com.listaih.app.data.local.entity.UserEntity

@Database(
    entities = [
        ShoppingListEntity::class,
        ListItemEntity::class,
        ProductEntity::class,
        PriceEntryEntity::class,
        HouseholdEntity::class,
        UserEntity::class,
        SyncQueueEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun listItemDao(): ListItemDao
    abstract fun productDao(): ProductDao
    abstract fun priceEntryDao(): PriceEntryDao
    abstract fun householdDao(): HouseholdDao
    abstract fun userDao(): UserDao
    abstract fun syncQueueDao(): SyncQueueDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "listaih_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}