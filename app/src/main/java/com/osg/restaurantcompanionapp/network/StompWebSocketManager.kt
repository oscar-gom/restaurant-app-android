package com.osg.restaurantcompanionapp.network

import com.google.gson.Gson
import com.osg.restaurantcompanionapp.model.Order
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.subscribe
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient

class StompWebSocketManager(
    private val onOrderReceived: (Order) -> Unit
) {
    private val gson = Gson()
    private var stompSession: StompSession? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private var subscriptionJob: Job? = null

    suspend fun connect(url: String, topic: String) {
        try {
            val webSocketClient = OkHttpWebSocketClient()
            val stompClient = StompClient(webSocketClient)

            stompSession = stompClient.connect(url)

            subscribeTo(topic)

        } catch (_: Exception) {
        }
    }

    private suspend fun subscribeTo(topic: String) {
        try {
            val session = stompSession ?: return

            subscriptionJob = session.subscribe(topic)
                .onEach { frame ->
                    val message = frame.bodyAsText

                    try {
                        val order = gson.fromJson(message, Order::class.java)
                        onOrderReceived(order)
                    } catch (_: Exception) {
                    }
                }
                .catch { _ -> }
                .launchIn(scope)

        } catch (_: Exception) {
        }
    }

    suspend fun disconnect() {
        try {
            subscriptionJob?.cancel()
            stompSession?.disconnect()
        } catch (_: Exception) {
        }
    }
}
