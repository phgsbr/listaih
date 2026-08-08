package com.listaih.app.sync

import android.content.Context
import android.util.Log
import com.listaih.app.data.preferences.AppPreferences
import com.listaih.app.data.repository.ShoppingRepository
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Singleton
class SocketSyncService @Inject constructor(
    private val context: Context,
    private val appPreferences: AppPreferences,
    private val repository: ShoppingRepository
) {

    private val TAG = "SocketSyncService"
    private var socket: Socket? = null
    private var isConnected = false
    private val scope = CoroutineScope(Dispatchers.IO)

    fun connect() {
        scope.launch {
            try {
                val baseUrl = appPreferences.getBaseUrl().blockingFirst() ?: "http://10.0.2.2:3000"
                val accessToken = appPreferences.getAccessToken().blockingFirst()

                val opts = IO.Options().apply {
                    transports = arrayOf("websocket", "polling")
                    reconnection = true
                    reconnectionAttempts = 5
                    reconnectionDelay = 1000
                    query = "token=$accessToken"
                }

                socket = IO.socket("$baseUrl", opts)

                socket?.on(Socket.EVENT_CONNECT, Emitter.Listener { args ->
                    isConnected = true
                    Log.d(TAG, "Socket connected")
                    joinHousehold()
                })

                socket?.on(Socket.EVENT_DISCONNECT, Emitter.Listener { args ->
                    isConnected = false
                    Log.d(TAG, "Socket disconnected: ${args[0]}")
                })

                socket?.on("item_added", Emitter.Listener { args ->
                    handleItemAdded(args[0] as Map<String, Any>)
                })

                socket?.on("item_updated", Emitter.Listener { args ->
                    handleItemUpdated(args[0] as Map<String, Any>)
                })

                socket?.on("item_removed", Emitter.Listener { args ->
                    handleItemRemoved(args[0] as Map<String, Any>)
                })

                socket?.on("list_created", Emitter.Listener { args ->
                    handleListCreated(args[0] as Map<String, Any>)
                })

                socket?.on("list_updated", Emitter.Listener { args ->
                    handleListUpdated(args[0] as Map<String, Any>)
                })

                socket?.on("list_deleted", Emitter.Listener { args ->
                    handleListDeleted(args[0] as Map<String, Any>)
                })

                socket?.connect()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to connect socket: ${e.message}")
            }
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
        isConnected = false
    }

    private fun joinHousehold() {
        val householdId = appPreferences.getHouseholdId().blockingFirst()
        householdId?.let { id ->
            socket?.emit("join_household", id)
        }
    }

    private fun handleItemAdded(data: Map<String, Any>) {
        // Handle real-time item added
        // TODO: Update local database and notify UI
    }

    private fun handleItemUpdated(data: Map<String, Any>) {
        // Handle real-time item updated
    }

    private fun handleItemRemoved(data: Map<String, Any>) {
        // Handle real-time item removed
    }

    private fun handleListCreated(data: Map<String, Any>) {
        // Handle real-time list created
    }

    private fun handleListUpdated(data: Map<String, Any>) {
        // Handle real-time list updated
    }

    private fun handleListDeleted(data: Map<String, Any>) {
        // Handle real-time list deleted
    }

    fun isSocketConnected(): Boolean = isConnected
}