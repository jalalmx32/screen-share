package com.screenshare.app.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.nio.ByteBuffer

/**
 * WebSocket client for connecting to ScreenShare server
 */
class ScreenShareClient(
    private val onFrameReceived: (Bitmap) -> Unit,
    private val onConnectionChanged: (ConnectionState) -> Unit
) {
    
    sealed class ConnectionState {
        object Disconnected : ConnectionState()
        object Connecting : ConnectionState()
        data class Connected(val serverInfo: ServerInfo? = null) : ConnectionState()
        data class Error(val message: String) : ConnectionState()
    }
    
    data class ServerInfo(
        val clientId: String,
        val resolution: Pair<Int, Int>,
        val fps: Int
    )
    
    private var webSocket: WebSocketClient? = null
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private val _stats = MutableStateFlow(Stats())
    val stats: StateFlow<Stats> = _stats.asStateFlow()
    
    data class Stats(
        val fps: Int = 0,
        val frameCount: Long = 0,
        val latencyMs: Long = 0
    )
    
    private var frameCount = 0L
    private var lastFpsTime = System.currentTimeMillis()
    private var currentFps = 0
    
    /**
     * Connect to the server
     */
    fun connect(host: String, port: Int) {
        disconnect()
        
        _connectionState.value = ConnectionState.Connecting
        
        try {
            val uri = URI("ws://$host:$port")
            
            webSocket = object : WebSocketClient(uri) {
                override fun onOpen(handshake: ServerHandshake?) {
                    _connectionState.value = ConnectionState.Connected()
                    onConnectionChanged(ConnectionState.Connected())
                }
                
                override fun onMessage(message: String?) {
                    // Handle JSON messages (welcome, stats)
                    message?.let { handleJsonMessage(it) }
                }
                
                override fun onMessage(message: ByteBuffer?) {
                    // Handle binary frames (JPEG images)
                    message?.let { handleBinaryMessage(it) }
                }
                
                override fun onClose(code: Int, reason: String?, remote: Boolean) {
                    _connectionState.value = ConnectionState.Disconnected
                    onConnectionChanged(ConnectionState.Disconnected)
                }
                
                override fun onError(ex: Exception?) {
                    val errorMsg = ex?.message ?: "Unknown error"
                    _connectionState.value = ConnectionState.Error(errorMsg)
                    onConnectionChanged(ConnectionState.Error(errorMsg))
                }
            }
            
            webSocket?.connect()
            
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.Error(e.message ?: "Connection failed")
            onConnectionChanged(ConnectionState.Error(e.message ?: "Connection failed"))
        }
    }
    
    /**
     * Disconnect from server
     */
    fun disconnect() {
        try {
            webSocket?.close()
            webSocket = null
            _connectionState.value = ConnectionState.Disconnected
            onConnectionChanged(ConnectionState.Disconnected)
        } catch (e: Exception) {
            // Ignore close errors
        }
    }
    
    /**
     * Handle JSON messages from server
     */
    private fun handleJsonMessage(message: String) {
        try {
            val json = org.json.JSONObject(message)
            val type = json.optString("type", "")
            
            when (type) {
                "welcome" -> {
                    val clientId = json.optString("client_id", "")
                    val resolution = json.optJSONArray("resolution")
                    val fps = json.optInt("fps", 30)
                    
                    val width = resolution?.optInt(0) ?: 1280
                    val height = resolution?.optInt(1) ?: 720
                    
                    val serverInfo = ServerInfo(
                        clientId = clientId,
                        resolution = Pair(width, height),
                        fps = fps
                    )
                    
                    _connectionState.value = ConnectionState.Connected(serverInfo)
                    onConnectionChanged(ConnectionState.Connected(serverInfo))
                }
                
                "stats" -> {
                    val fps = json.optInt("fps", 0)
                    val frame = json.optLong("frame", 0)
                    
                    _stats.value = Stats(
                        fps = fps,
                        frameCount = frame
                    )
                }
            }
        } catch (e: Exception) {
            // Ignore parse errors
        }
    }
    
    /**
     * Handle binary frame messages
     */
    private fun handleBinaryMessage(buffer: ByteBuffer) {
        try {
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            
            // Decode JPEG to Bitmap
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            
            if (bitmap != null) {
                // Update frame count
                frameCount++
                
                // Calculate FPS
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastFpsTime >= 1000) {
                    currentFps = ((frameCount - _stats.value.frameCount) * 1000 / (currentTime - lastFpsTime)).toInt()
                    lastFpsTime = currentTime
                }
                
                // Notify listener
                onFrameReceived(bitmap)
            }
        } catch (e: Exception) {
            // Ignore decode errors
        }
    }
    
    /**
     * Check if connected
     */
    fun isConnected(): Boolean {
        return _connectionState.value is ConnectionState.Connected
    }
    
    /**
     * Get current connection state
     */
    fun getConnectionState(): ConnectionState {
        return _connectionState.value
    }
}
