package com.screenshare.app

import android.app.Activity
import android.graphics.Bitmap
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ipInput = findViewById(R.id.ipInput)
        connectBtn = findViewById(R.id.connectBtn)
        disconnectBtn = findViewById(R.id.disconnectBtn)
        statusText = findViewById(R.id.statusText)
        fpsText = findViewById(R.id.fpsText)
        screenView = findViewById(R.id.screenView)

        connectBtn.setOnClickListener { connect() }
        disconnectBtn.setOnClickListener { disconnect() }
    }

    private fun connect() {
        val address = ipInput.text.toString().trim()
        if (address.isEmpty()) {
            updateStatus("Enter IP address", 0xFFFFB800.toInt())
            return
        }

        // Disconnect existing connection first
        disconnect()

        updateStatus("Connecting...", 0xFFFFB800.toInt())
        connectBtn.isEnabled = false

        // Connect in background thread
        Thread {
            try {
                // Add ws:// if not present
                val url = if (address.startsWith("ws://")) address else "ws://$address"
                Log.d(TAG, "Connecting to: $url")
                
                val uri = URI(url)
                
                webSocket = object : WebSocketClient(uri) {
                    override fun onOpen(handshake: ServerHandshake?) {
                        Log.d(TAG, "Connected to server")
                        runOnUiThread {
                            updateStatus("Connected", 0xFF4ADE80.toInt())
                            connectBtn.visibility = View.GONE
                            disconnectBtn.visibility = View.VISIBLE
                            screenView.visibility = View.VISIBLE
                            ipInput.isEnabled = false
                            connectBtn.isEnabled = true
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
                                            fpsText.text = "FPS: $fps"
                                        }
                                        frameCount = 0
                                        lastFpsTime = currentTime
                                    }
                                    
                                    mainHandler.post {
                                        screenView.setImageBitmap(bitmap)
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error processing frame", e)
                        }
                    }

                    override fun onMessage(message: String?) {
                        Log.d(TAG, "Text message: $message")
                    }

                    override fun onClose(code: Int, reason: String?, remote: Boolean) {
                        Log.d(TAG, "Disconnected: $reason")
                        runOnUiThread {
                            updateStatus("Disconnected", 0xFFF87171.toInt())
                            resetUI()
                        }
                    }

                    override fun onError(ex: Exception?) {
                        Log.e(TAG, "WebSocket error", ex)
                        runOnUiThread {
                            updateStatus("Error: ${ex?.message}", 0xFFF87171.toInt())
                            resetUI()
                        }
                    }
                }

                webSocket?.connectBlocking()

            } catch (e: Exception) {
                Log.e(TAG, "Connection failed", e)
                runOnUiThread {
                    updateStatus("Error: ${e.message}", 0xFFF87171.toInt())
                    resetUI()
                }
            }
        }.start()
    }

    private fun disconnect() {
        try {
            webSocket?.close()
            webSocket = null
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting", e)
        }
        runOnUiThread {
            resetUI()
            updateStatus("Offline", 0xFFF87171.toInt())
        }
    }

    private fun updateStatus(text: String, color: Int) {
        statusText.text = "● $text"
        statusText.setTextColor(color)
    }

    private fun resetUI() {
        connectBtn.visibility = View.VISIBLE
        disconnectBtn.visibility = View.GONE
        screenView.visibility = View.GONE
        ipInput.isEnabled = true
        connectBtn.isEnabled = true
        fpsText.text = "FPS: 0"
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnect()
    }
}
