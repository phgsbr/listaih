package com.listaih.app.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.listaih.app.data.local.AppDatabase
import com.listaih.app.data.local.dao.SyncQueueDao
import com.listaih.app.data.local.entity.SyncQueueEntity
import com.listaih.app.data.network.ApiService
import com.listaih.app.data.preferences.AppPreferences
import com.listaih.app.data.repository.ShoppingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import dagger.hilt.android.HiltWorkerFactory
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPoint
import dagger.hilt.android.InstallIn
import dagger.hilt.components.SingletonComponent

@AndroidEntryPoint
class SyncWorker @Inject constructor(
    context: Context,
    params: WorkerParameters,
    private val database: AppDatabase,
    private val apiService: ApiService,
    private val appPreferences: AppPreferences,
    private val repository: ShoppingRepository
) : CoroutineWorker(context, params) {

    private val syncQueueDao: SyncQueueDao = database.syncQueueDao()
    private val TAG = "SyncWorker"

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val accessToken = appPreferences.getAccessToken().blockingFirst()
            if (accessToken == null) {
                Log.d(TAG, "No access token, skipping sync")
                return@withContext Result.success()
            }

            val pendingSync = syncQueueDao.getPendingSync()
            if (pendingSync.isEmpty()) {
                Log.d(TAG, "No pending sync items")
                return@withContext Result.success()
            }

            Log.d(TAG, "Processing ${pendingSync.size} pending sync items")

            var hasFailures = false

            for (item in pendingSync) {
                try {
                    when (item.operation) {
                        "create" -> processCreate(item)
                        "update" -> processUpdate(item)
                        "delete" -> processDelete(item)
                    }
                    syncQueueDao.delete(item)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync item ${item.id}: ${e.message}")
                    syncQueueDao.incrementRetryCount(item.id)
                    hasFailures = true
                }
            }

            if (hasFailures) {
                Result.retry()
            } else {
                Result.success()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sync worker failed: ${e.message}")
            Result.retry()
        }
    }

    private suspend fun processCreate(item: SyncQueueEntity) {
        when (item.entityType) {
            "list" -> {
                // Parse payload and call API
                // TODO: Implement
            }
            "item" -> {
                // Parse payload and call API
                // TODO: Implement
            }
        }
    }

    private suspend fun processUpdate(item: SyncQueueEntity) {
        // TODO: Implement
    }

    private suspend fun processDelete(item: SyncQueueEntity) {
        // TODO: Implement
    }
}

@InstallIn(SingletonComponent::class)
@EntryPoint
interface SyncWorkerEntryPoint {
    fun syncWorker(): SyncWorker
}