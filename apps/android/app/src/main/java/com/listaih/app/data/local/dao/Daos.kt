package com.listaih.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.listaih.app.data.local.entity.ListItemEntity
import com.listaih.app.data.local.entity.ShoppingListEntity
import com.listaih.app.data.local.entity.ShoppingListWithCounts
import com.listaih.app.data.local.entity.ProductEntity
import com.listaih.app.data.local.entity.PriceEntryEntity
import com.listaih.app.data.local.entity.HouseholdEntity
import com.listaih.app.data.local.entity.UserEntity
import com.listaih.app.data.local.entity.SyncQueueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(lists: List<ShoppingListEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(list: ShoppingListEntity)

    @Update
    suspend fun update(list: ShoppingListEntity)

    @Delete
    suspend fun delete(list: ShoppingListEntity)

    @Query("SELECT * FROM shopping_lists WHERE householdId = :householdId AND archivedAt IS NULL ORDER BY updatedAt DESC")
    fun getActiveLists(householdId: String): Flow<List<ShoppingListEntity>>

    @Query("SELECT * FROM shopping_lists WHERE householdId = :householdId AND archivedAt IS NOT NULL ORDER BY archivedAt DESC")
    fun getArchivedLists(householdId: String): Flow<List<ShoppingListEntity>>

    @Query("SELECT * FROM shopping_lists WHERE id = :id")
    fun getListById(id: String): Flow<ShoppingListEntity?>

    @Query("SELECT * FROM shopping_lists WHERE householdId = :householdId")
    suspend fun getAllLists(householdId: String): List<ShoppingListEntity>

    @Query("SELECT * FROM shopping_lists")
    suspend fun getAll(): List<ShoppingListEntity>

    @Query("SELECT * FROM shopping_lists WHERE serverSynced = 0")
    suspend fun getUnsyncedLists(): List<ShoppingListEntity>

    @Query("UPDATE shopping_lists SET serverSynced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)

    @Query("""
        SELECT
            sl.*,
            COALESCE(SUM(CASE WHEN li.checked THEN 1 ELSE 0 END), 0) as checkedCount,
            COUNT(li.id) as totalCount,
            COALESCE(SUM(CASE WHEN li.checked THEN COALESCE(li.actualPrice, li.estimatedPrice, 0) * li.quantity ELSE 0 END), 0) as spentTotal,
            COALESCE(SUM(COALESCE(li.estimatedPrice, 0) * li.quantity), 0) as estimatedTotal
        FROM shopping_lists sl
        LEFT JOIN list_items li ON li.listId = sl.id
        WHERE sl.householdId = :householdId AND sl.archivedAt IS NULL
        GROUP BY sl.id
        ORDER BY sl.updatedAt DESC
    """)
    fun getActiveListsWithCounts(householdId: String): Flow<List<ShoppingListWithCounts>>
}

@Dao
interface ListItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ListItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ListItemEntity)

    @Update
    suspend fun update(item: ListItemEntity)

    @Delete
    suspend fun delete(item: ListItemEntity)

    @Query("SELECT * FROM list_items WHERE listId = :listId ORDER BY position ASC, createdAt ASC")
    fun getItemsByListId(listId: String): Flow<List<ListItemEntity>>

    @Query("SELECT * FROM list_items WHERE id = :id")
    fun getItemById(id: String): Flow<ListItemEntity?>

    @Query("SELECT * FROM list_items")
    suspend fun getAllItems(): List<ListItemEntity>

    @Query("SELECT * FROM list_items WHERE listId = :listId AND serverSynced = 0")
    suspend fun getUnsyncedItems(listId: String): List<ListItemEntity>

    @Query("UPDATE list_items SET serverSynced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)

    @Query("UPDATE list_items SET checked = :checked, checkedBy = :checkedBy, checkedAt = :checkedAt, updatedAt = :updatedAt, actualPrice = CASE WHEN :checked THEN estimatedPrice * quantity ELSE actualPrice END WHERE id = :id")
    suspend fun updateCheckedStatus(id: String, checked: Boolean, checkedBy: String?, checkedAt: Long, updatedAt: Long)
}

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: ProductEntity)

    @Query("SELECT * FROM products WHERE name LIKE :query ORDER BY name ASC LIMIT 20")
    suspend fun searchProducts(query: String): List<ProductEntity>

    @Query("SELECT * FROM products WHERE category = :category ORDER BY name ASC")
    suspend fun getProductsByCategory(category: String): List<ProductEntity>

    @Query("SELECT DISTINCT category FROM products WHERE category IS NOT NULL ORDER BY category ASC")
    suspend fun getAllCategories(): List<String>
}

@Dao
interface PriceEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<PriceEntryEntity>)

    @Query("SELECT * FROM price_entries WHERE productId = :productId ORDER BY date DESC LIMIT 10")
    suspend fun getRecentPrices(productId: String): List<PriceEntryEntity>
}

@Dao
interface HouseholdDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(household: HouseholdEntity)

    @Query("SELECT * FROM households WHERE id = :id")
    suspend fun getById(id: String): HouseholdEntity?

    @Query("SELECT * FROM households WHERE inviteCode = :code")
    suspend fun getByInviteCode(code: String): HouseholdEntity?
}

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getById(id: String): UserEntity?
}

@Dao
interface SyncQueueDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: SyncQueueEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<SyncQueueEntity>)

    @Query("SELECT * FROM sync_queue ORDER BY createdAt ASC LIMIT 50")
    suspend fun getPendingSync(): List<SyncQueueEntity>

    @Delete
    suspend fun delete(item: SyncQueueEntity)

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("UPDATE sync_queue SET retryCount = retryCount + 1 WHERE id = :id")
    suspend fun incrementRetryCount(id: Int)
}