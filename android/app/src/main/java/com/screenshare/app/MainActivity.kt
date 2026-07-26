package com.screenshare.app

import android.app.Activity
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.nio.ByteBuffer

class MainActivity : Activity() {

    private val TAG = "ScreenShare"
    
    private lateinit var ipInput: EditText
    private lateinit var connectBtn: Button
    private lateinit var disconnectBtn: Button
    private lateinit var statusText: TextView
    private lateinit var fpsText: TextView
    private lateinit var screenView: ImageView

    private var webSocket: WebSocketClient? = null
    private var frameCount = 0L
    private var lastFpsTime = System.currentTimeMillis()
    private val mainHandler = Handler(Looper.getMainLooper())
    private var isConnecting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

            ipInput = findViewById(R.id.ipInput)
            connectBtn = findViewById(R.id.connectBtn)
            disconnectBtn = findViewById(R.id.disconnectBtn)
            statusText = findViewById(R.id.statusText)
            fpsText = findViewById(R.id.fpsText)
            screenView = findViewById(R.id.screenView)

            connectBtn.setOnClickListener { 
                if (!isConnecting) connect() 
            }
            disconnectBtn.setOnClickListener { disconnect() }
            
            Log.d(TAG, "App started")
        } catch (e: Exception) {
            Log.e(TAG, "onCreate error", e)
        }
    }

    private fun connect() {
        val address = ipInput.text.toString().trim()
        if (address.isEmpty()) {
            showStatus("Enter IP address", 0xFFFFB800.toInt())
            return
        }

        disconnect()
        isConnecting = true
        showStatus("Connecting...", 0xFFFFB800.toInt())
        connectBtn.isEnabled = false

        Thread {
            try {
                val url = if (address.startsWith("ws://") || address.startsWith("wss://")) {
                    address
                } else {
                    "ws://$address"
                }
                
                Log.d(TAG, "Connecting to: $url")
                val uri = URI(url)
                
                webSocket = object : WebSocketClient(uri) {
                    override fun onOpen(handshake: ServerHandshake?) {
                        Log.d(TAG, "Connected")
                        mainHandler.post {
                            showStatus("Connected", 0xFF4ADE80.toInt())
                            connectBtn.visibility = View.GONE
                            disconnectBtn.visibility = View.VISIBLE
                            screenView.visibility = View.VISIBLE
                            ipInput.isEnabled = false
                            isConnecting = false
                        }
                    }

                    override fun onMessage(message: ByteBuffer?) {
                        try {
                            message?.let {
                                val bytes = ByteArray(it.remaining())
                                it.get(bytes)
                                
                                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                if (bitmap != null) {
                                    frameCount++
                                    val currentTime = System.currentTimeMillis()
                                    if (currentTime - lastFpsTime >= 1000) {
                                        val fps = ((frameCount) * 1000 / (currentTime - lastFpsTime)).toInt()
                                        mainHandler.post {
                                            try { fpsText.text = "FPS: $fps" } catch (e: Exception) {}
                                        }
                                        frameCount = 0
                                        lastFpsTime = currentTime
                                    }
                                    
                                    mainHandler.post {
                                        try { screenView.setImageBitmap(bitmap) } catch (e: Exception) {}
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Frame error", e)
                        }
                    }

                    override fun onMessage(message: String?) {
                        Log.d(TAG, "Text: $message")
                    }

                    override fun onClose(code: Int, reason: String?, remote: Boolean) {
                        Log.d(TAG, "Disconnected: $code")
                        mainHandler.post {
                            showStatus("Disconnected", 0xFFF87171.toInt())
                            resetUI()
                        }
                    }

                    override fun onError(ex: Exception?) {
                        Log.e(TAG, "Error: ${ex?.message}", ex)
                        mainHandler.post {
                            showStatus("Error: ${ex?.localizedMessage}", 0xFFF87171.toInt())
                            resetUI()
                        }
                    }
                }

                webSocket?.connectBlocking()

            } catch (e: Exception) {
                Log.e(TAG, "Connection failed", e)
                mainHandler.post {
                    showStatus("Failed: ${e.localizedMessage}", 0xFFF87171.toInt())
                    resetUI()
                }
            }
        }.start()
    }

    private fun disconnect() {
        try { webSocket?.close() } catch (e: Exception) {}
        webSocket = null
        mainHandler.post {
            resetUI()
            showStatus("Offline", 0xFFF87171.toInt())
        }
    }

    private fun showStatus(text: String, color: Int) {
        try {
            statusText.text = "● $text"
            statusText.setTextColor(color)
        } catch (e: Exception) {}
    }

    private fun resetUI() {
        try {
            connectBtn.visibility = View.VISIBLE
            connectBtn.isEnabled = true
            disconnectBtn.visibility = View.GONE
            screenView.visibility = View.GONE
            ipInput.isEnabled = true
            fpsText.text = "FPS: 0"
            isConnecting = false
        } catch (e: Exception) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        try { webSocket?.close(); webSocket = null } catch (e: Exception) {}
    }
}
